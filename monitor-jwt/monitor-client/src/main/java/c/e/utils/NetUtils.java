package c.e.utils;

import c.e.entity.ConnectionConfig;
import c.e.entity.Response;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//网络工具，用于发起请求
@Slf4j
@Component
public class NetUtils {

    //因为没有导入外部的网络依赖，就使用JDK提供的网络工具
    //HttpClient是由JDK11提供的
    private final HttpClient client = HttpClient.newHttpClient();

    @Lazy    //懒加载，在调用的时候再去注入，不加这个的话可能会产生循环引用的问题
    @Resource
    ConnectionConfig config;

    //定制超时时间什么的后面再设置
    //注册服务器
    public boolean registerToServer(String address,String token) {
        log.info("正在向服务端注册，请稍后...");
        Response response = this.doGet("/register", address, token);
        if (response.success()){
            log.info("客户端注册已完成!");
        }else {
            log.error("客户端注册失败: {}",response.message());
        }
        return response.success();
    }

    //重载一下方法,这次从配置文件中读取
    private Response doGet(String url){
        return this.doGet(url,config.getAddress(),config.getToken());
    }

    //用于内部发起请求的
    private Response doGet(String url,String address,String token){
        try{
            //发送请求
            HttpRequest request = HttpRequest.newBuilder().GET()
                    .uri(new URI(address + "/monitor" + url))
                    .header("Authorization",token)
                    .build();
            //创建对象
            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            return JSONObject.parseObject(response.body()).to(Response.class);
        }catch (Exception e){
            log.error("在发起服务端请求时出现问题",e);
            return Response.errorResponse(e);
        }
    }

}
