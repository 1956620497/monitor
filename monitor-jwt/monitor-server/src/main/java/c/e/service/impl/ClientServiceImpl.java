package c.e.service.impl;

import c.e.entity.dto.Client;
import c.e.entity.dto.ClientDetail;
import c.e.entity.dto.ClientSsh;
import c.e.entity.vo.request.*;
import c.e.entity.vo.response.*;
import c.e.mapper.ClientMapper;
import c.e.mapper.ClientDetailMapper;
import c.e.mapper.ClientSshMapper;
import c.e.service.ClientService;
import c.e.utils.InfluxDbUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//注册客户端
@Service
public class ClientServiceImpl extends ServiceImpl<ClientMapper, Client> implements ClientService {

    //重新生成一个Token
    private String registerToken = this.generateNewToken();

    //将Token返回给前端
    @Override
    public String registerToken() {
        return registerToken;
    }

    //做一个缓存，不用每次都去找数据库读取
    //根据id查找
    //推荐使用ConcurrentHashMap，如果客户端一多，上报数据的频率高，查询也频繁，默认的hashMap在高并发情况下有可能出问题
    //使用ConcurrentHashMap会稍微好一些
    private final Map<Integer,Client> clientIdCache = new ConcurrentHashMap<>();
    //根据token查找
    private final Map<String,Client> clientTokenCache = new ConcurrentHashMap<>();

    //client对象的mapper
    @Resource
    ClientDetailMapper detailMapper;

    //InfluxDB数据库
    @Resource
    InfluxDbUtils influx;

    @Resource
    ClientSshMapper sshMapper;

    //初始化方法，在第一时间调用该方法查出数据并放入缓存
    @PostConstruct
    public void initClientCache(){
        //清空一下缓存,因为删除主机时，原有的缓存是不会自动删除的
        clientTokenCache.clear();
        clientIdCache.clear();
        //将所有的客户端查出来
        this.list().forEach(this::addClientCache);
    }

    //根据id查找客户端
    @Override
    public Client findClientById(int id) {
        return clientIdCache.get(id);
    }

    //根据token查找客户端
    @Override
    public Client findClientByToken(String token) {
        return clientTokenCache.get(token);
    }

    //验证并注册
    @Override
    public boolean verifyAndRegister(String token) {
        //判断一下，传入的token是否与
        if (this.registerToken.equals(token)) {
            //生成一个ID
            int id = this.randomClientId();
            //创建客户端实体对象
            Client client = new Client(id,"未命名主机",token,"cn","未命名节点",new Date());
            if (this.save(client)) {
                //如果插入成功,重新生成一条token
                registerToken = this.generateNewToken();
                this.addClientCache(client);
                return true;
            }
        }
        return false;
    }

    //保存上报数据
    @Override
    public void updateClientDetail(ClientDetailVO vo, Client client) {
        //详细数据对象
        ClientDetail detail= new ClientDetail();
        BeanUtils.copyProperties(vo,detail);
        detail.setId(client.getId());
        //判断一下
        if (Objects.nonNull(detailMapper.selectById(client.getId()))){
            //如果有数据，就更新
            detailMapper.updateById(detail);
        }else{
            //没有数据，就插入一条数据
            detailMapper.insert(detail);
        }
    }

    //存储最新的数据
    private Map<Integer,RuntimeDetailVO> currentRuntime = new ConcurrentHashMap<>();

    //存储实时上报数据
    @Override
    public void updateRuntimeDetail(RuntimeDetailVO vo, Client client) {
//        //逻辑:如果有新的数据来了，先将currentRuntime中的数据存入数据库，再将数据存入currentRuntime中
//        RuntimeDetailVO old = currentRuntime.put(client.getId(), vo);  //返回的数据old是老数据
//        //将数据存入数据库
//        if (old != null){
//            //为空的情况有可能是刚启动，判断一下是否有数据
//            //如果有数据，就调用方法，将数据存入InfluxDB数据库中
//            influx.writeRuntimeData(client.getId(),old);
//        }
//        //如果想要立即将数据存入数据库，也可以直接让数据进入数据库

        //改:同时往两个存储对象中存储数据
        currentRuntime.put(client.getId(),vo);
        influx.writeRuntimeData(client.getId(),vo);
    }

    //获取主机列表
    @Override
    public List<ClientPreviewVO> listClients() {
        if (clientIdCache.values().isEmpty())
            return null;

        //取出所有缓存
        return clientIdCache.values().stream().map(client -> {
            ClientPreviewVO vo = client.asViewObject(ClientPreviewVO.class);
            //从数据库中获取数据,基本上报数据
            BeanUtils.copyProperties(detailMapper.selectById(vo.getId()),vo);
            //运行时数据
            RuntimeDetailVO runtime = currentRuntime.get(client.getId());
            //如果能拿到,判断该数据的上报时间   如果拿到的数据超过十秒钟之前，说明主机离线了
            //网络有突然丢包的情况，如果拿到的数据是60秒之前的，说明主机离线了
            //如果有数据，并且在60秒内上报的数据，则该主机是在线的
            if (this.isOnline(runtime)){
                BeanUtils.copyProperties(runtime,vo);
                vo.setOnline(true);
            }
            return vo;
        }).toList();
    }

    //获取简单主机列表
    @Override
    public List<ClientSimpleVO> listSimpleList() {
        return clientIdCache.values().stream().map(client -> {
            ClientSimpleVO vo = client.asViewObject(ClientSimpleVO.class);
            BeanUtils.copyProperties(detailMapper.selectById(vo.getId()),vo);
            return vo;
        }).toList();
    }

    //修改主机名称
    @Override
    public void renameClient(RenameClientVO vo) {
        //修改主机名字
        this.update(Wrappers.<Client>update().eq("id",vo.getId()).set("name",vo.getName()));
        //重新初始化一下
        this.initClientCache();
    }

    //获取主机详细信息
    @Override
    public ClientDetailsVO clientDetails(int clientId) {
        //首先从缓存中读取数据
        ClientDetailsVO vo = this.clientIdCache.get(clientId).asViewObject(ClientDetailsVO.class);
        //从数据库中查询数据并取出放入vo对象
        BeanUtils.copyProperties(detailMapper.selectById(clientId),vo);
        //获取当前主机是否离线
        vo.setOnline(this.isOnline(currentRuntime.get(clientId)));
        return vo;
    }

    //更改主机节点
    @Override
    public void renameNode(RenameNodeVO vo) {
        //修改主机节点
        this.update(Wrappers.<Client>update().eq("id",vo.getId())
                .set("node",vo.getNode()).set("location",vo.getLocation()));
        //重新初始化一下
        this.initClientCache();
    }

    //判断是否是在线状态
    private boolean isOnline(RuntimeDetailVO runtime){
        //如果能拿到,判断该数据的上报时间   如果拿到的数据超过十秒钟之前，说明主机离线了
        //网络有突然丢包的情况，如果拿到的数据是60秒之前的，说明主机离线了
        //如果有数据，并且在60秒内上报的数据，则该主机是在线的
        return runtime != null && System.currentTimeMillis() - runtime.getTimestamp() < 60 * 1000;
    }

    //获取历史的数据
    @Override
    public RuntimeHistoryVO clientRuntimeDetailsHistory(int clientId) {
        //读取出InfluxDB数据库中的数据
        RuntimeHistoryVO vo = influx.readRuntimeData(clientId);
        //补充内存容量和磁盘容量
        ClientDetail detail = detailMapper.selectById(clientId);
        BeanUtils.copyProperties(detail,vo);
        return vo;
    }

    //获取当前的数据
    @Override
    public RuntimeDetailVO clientRuntimeDetailsNow(int clientId) {
        return currentRuntime.get(clientId);
    }

    //删除主机
    @Override
    public void deleteClient(int clientId) {
        //数据库删除客户端id
        this.removeById(clientId);
        //详细信息删除
        detailMapper.deleteById(clientId);
        //刷新缓存
        this.initClientCache();
        //运行时数据也删除掉
        currentRuntime.remove(clientId);
        //InfluxDB数据库不用删除，因为一天后数据会自动删除，所以没有删除的必要
    }

    //保存ssh连接信息
    @Override
    public void saveClientSshConnection(SshConnectionVO vo) {
        //先从缓存中取
        Client client = clientIdCache.get(vo.getId());
        if (client == null) return;
        ClientSsh ssh = new ClientSsh();
        BeanUtils.copyProperties(vo,ssh);
//        ClientDetail detail = detailMapper.selectById(vo.getId());
//        vo.setIp(detail.getIp());
        if (Objects.nonNull(sshMapper.selectById(client.getId()))){
            sshMapper.updateById(ssh);
        }else {
            sshMapper.insert(ssh);
        }
    }

    //获取ssh连接信息
    @Override
    public SshSettingsVO sshSettings(int clientId) {
        ClientDetail detail = detailMapper.selectById(clientId);
        ClientSsh ssh = sshMapper.selectById(clientId);
        SshSettingsVO vo;
        if (ssh == null){
            vo = new SshSettingsVO();
        }else {
            vo = ssh.asViewObject(SshSettingsVO.class);
        }
        vo.setIp(detail.getIp());
        return vo;
    }

    //新增缓存方法
    private void addClientCache(Client client){
        clientIdCache.put(client.getId(),client);
        clientTokenCache.put(client.getToken(),client);
    }

    //生成一个8位的，随机的客户端id
    private int randomClientId(){
        return new Random().nextInt(90000000)+10000000;
    }

    //随机生成一个24位的字符串
    private String generateNewToken(){
        //从这个字符里，每次随机提取一个组成Token
        String CHARACTERS = "abcdefghijklmnopgrstuvwxyzABCDEFGHIJKLMNOPQRSTUVMXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(24);
        for (int i=0;i<24;i++)
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        return sb.toString();
    }

}
