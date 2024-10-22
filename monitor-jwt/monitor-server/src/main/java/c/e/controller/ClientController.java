package c.e.controller;

import c.e.entity.RestBean;
import c.e.entity.dto.Client;
import c.e.entity.vo.request.ClientDetailVO;
import c.e.entity.vo.request.RuntimeDetailVO;
import c.e.service.ClientService;
import c.e.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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

    //客户端上传基本信息
    @PostMapping("/detail")
    public RestBean<Void> updateClientDetails(@RequestAttribute(Const.ATTR_CLIENT) Client client,
                                              @RequestBody @Valid ClientDetailVO vo){
        service.updateClientDetail(vo,client);
        return RestBean.success();
    }

    //客户端上传实时信息上报
    @PostMapping("/runtime")
    public RestBean<Void> updateRuntimeDetails(@RequestAttribute(Const.ATTR_CLIENT)Client client,
                                               @RequestBody @Valid RuntimeDetailVO vo){
        service.updateRuntimeDetail(vo,client);
        return RestBean.success();
    }


}
