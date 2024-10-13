package c.e.service.impl;

import c.e.entity.dto.Client;
import c.e.mapper.ClientMapper;
import c.e.service.ClientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.Random;
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

    //初始化方法，在第一时间调用该方法查出数据并放入缓存
    @PostConstruct
    public void initClientCache(){
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
            Client client = new Client(id,"未命名主机",token,new Date());
            if (this.save(client)) {
                //如果插入成功,重新生成一条token
                registerToken = this.generateNewToken();
                this.addClientCache(client);
                return true;
            }
        }
        return false;
    }

    //更新缓存方法
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
        System.out.println(sb);
        return sb.toString();
    }
}
