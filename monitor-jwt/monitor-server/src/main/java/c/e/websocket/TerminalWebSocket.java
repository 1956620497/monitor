package c.e.websocket;


import c.e.entity.dto.ClientDetail;
import c.e.entity.dto.ClientSsh;
import c.e.mapper.ClientDetailMapper;
import c.e.mapper.ClientSshMapper;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//与web页面建立长连接
//这个类每来一个连接都会创建一个新的实例
@Slf4j
@Component
@ServerEndpoint("/terminal/{clientId}")   //前面参数是接口的地址，后面跟上的参数是服务器的id
public class TerminalWebSocket {

    //这样写是错误的，注入不进来
//    @Resource
//    ClientDetailMapper detailMapper;
//    @Resource
//    ClientSshMapper sshMapper;

    //改成这样的写法,要通过这样的写法，将要注入的对象改成static变量
    private static ClientDetailMapper detailMapper;
    @Resource
    public void setDetailMapper(ClientDetailMapper detailMapper){
        TerminalWebSocket.detailMapper = detailMapper;
    }

    private static ClientSshMapper sshMapper;
    @Resource
    public void setSshMapper(ClientSshMapper sshMapper){
        TerminalWebSocket.sshMapper = sshMapper;
    }

    //设置一个map，保存所有建立的连接
    private static final Map<Session,Shell> sessionMap = new ConcurrentHashMap<>();

    //拿到客户端返回的信息
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    //当客户端发起打开连接
    @OnOpen
    public void onOpen(Session session,   //这个session对象是websocket中的
                       @PathParam(value = "clientId")String clientId) throws Exception{
        //判断一下用户有没有权限

        //先查询一下有没有主机，验证一下ssh连接信息是否完整
        ClientDetail detail = detailMapper.selectById(clientId);
        ClientSsh ssh = sshMapper.selectById(clientId);
        //这两个信息任意一个为空都不行
        if (detail == null || ssh == null){
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,"无法识别此主机"));
            return;
        }
        if (this.createSshConnection(session,ssh,detail.getIp())){
            log.info("主机 {} 的连接已创建",detail.getIp());
        }

    }


    //原理:在web端敲命令的时候，会通过websocket通过message的形式发送到服务器
    //这里处理一下命令
    //接收命令
    @OnMessage
    public void onMessage(Session session,String message) throws IOException {
        //通过session拿到对应的shell
        Shell shell = sessionMap.get(session);
        //拿到对应的输出流
        OutputStream output = shell.output;
        //将session放入输出流
        output.write(message.getBytes(StandardCharsets.UTF_8));
        //发送命令
        output.flush();
    }

    //关闭的时候要将资源释放掉
    @OnClose
    public void onClose(Session session) throws IOException {
        //获取一下连接
        Shell shell = sessionMap.get(session);
        if (shell != null){
            //如果有链接，执行关闭方法
            shell.close();
            //在缓存中移除
            sessionMap.remove(session);
            log.info("主机 {} 的SSH连接已断开",shell.js.getHost());
        }
    }

    //出现错误的处理
    @OnError
    public void onError(Session session,Throwable error) throws IOException {
        log.error("用户WebSocket连接出现错误",error);
        session.close();
    }


    //创建跟web页面的SSH连接
    private  boolean createSshConnection(Session session,ClientSsh ssh,String ip) throws IOException{
        try{
            //创建一个对象
            JSch jSch = new JSch();
            //这里的session不是webSocket中的session，这个是jsch自己的session
            com.jcraft.jsch.Session js = jSch.getSession(ssh.getUsername(), ip, ssh.getPort());
            //这个框架要求单独设置password
            js.setPassword(ssh.getPassword());
            //防止连接后断开连接
            js.setConfig("StrictHostKeyChecking","no");
            //连接超时时间,3秒钟
            js.setTimeout(3000);
            //正式连接客户端
            js.connect();
            //拿到新的对象，通过ChannelShell实现ssh的通信
            ChannelShell channel = (ChannelShell) js.openChannel("shell");
            //设置命令窗口颜色
            channel.setPtyType("xterm");
            //超时时间一秒钟，正式连接web端
            channel.connect(1000);
            //保存连接
            sessionMap.put(session,new Shell(session,js,channel));
            return true;
        }catch (JSchException e){
            //抛出各种各样的问题
            String message = e.getMessage();
            if (message.equals("Auth fail")){
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "登录SSH失败,用户名或密码错误"));
                log.error("连接SSH失败,用户名或密码错误,登录失败");
            }else if (message.contains("Connection refused")){
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "连接被拒绝,可能是没有启动SSH服务端或是开放端口"));
                log.error("连接SSH失败,连接被拒绝,可能是没有启动SSH服务或是开放端口");
            }else {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,message));
                log.error("连接SSH时出现错误",e);
            }
        }
        return false;
    }

    //封装一下
    //设置一个内部类用来保存对象
    private class Shell {
        private final Session session;
        private final com.jcraft.jsch.Session js;
        private final ChannelShell channel;
        //输入流
        private final InputStream input;
        //输出流
        private final OutputStream output;

        public Shell(Session session, com.jcraft.jsch.Session js,ChannelShell channel) throws IOException {
            this.js = js;
            this.session = session;
            this.channel = channel;
            this.input = channel.getInputStream();
            this.output = channel.getOutputStream();
            //提交一个任务
            service.submit(this::read);
        }

        //添加一个进程,一直从输出流中读取
        private void read(){
            try{
                byte[] buffer = new byte[1024 * 1024];
                int i;
                while ((i = input.read(buffer)) != -1){
                    String text = new String(Arrays.copyOfRange(buffer,0,i),StandardCharsets.UTF_8);
                    //拿到跟浏览器建立的session   然后将得到的输入结果返回回去
                    session.getBasicRemote().sendText(text);
                }
            }catch (Exception e){
                log.error("读取SSH输入流时出现问题",e);
            }
        }

        //关闭各项连接释放资源
        public void close() throws IOException {
            input.close();
            output.close();
            channel.disconnect();
            js.disconnect();
            service.shutdown();
        }
    }

}
