package c.e.service;

import c.e.entity.dto.Client;
import com.baomidou.mybatisplus.extension.service.IService;

//注册客户端
public interface ClientService extends IService<Client> {

    //将token给前端
    String registerToken();

    //根据id查找客户端
    Client findClientById(int id);

    //根据token查找客户端
    Client findClientByToken(String token);

    //验证并注册
    boolean verifyAndRegister(String token);
}
