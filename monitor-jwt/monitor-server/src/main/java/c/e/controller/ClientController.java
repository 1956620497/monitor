package c.e.controller;

import c.e.entity.RestBean;
import c.e.service.ClientService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//用于处理客户端的
@RestController
@RequestMapping("/monitor")
public class ClientController {

    @Resource
    ClientService service;

    //服务端注册
    //将token直接放到请求的Header
    @GetMapping("/register")
    public RestBean<Void> registerClient(@RequestHeader("Authorization")String token
                                         //客户端内部通信所使用的
                                         //注册时只需要请求头带有Token就可以了，不需要带其他参数，注册成功之后就可以知道它的信息了
                                         ){
        return service.verifyAndRegister(token) ?
                RestBean.success() : RestBean.failure(401,"客户端注册失败，请检查Token是否正确");

    }
}
