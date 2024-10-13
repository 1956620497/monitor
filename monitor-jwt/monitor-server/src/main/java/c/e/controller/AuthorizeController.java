package c.e.controller;

import c.e.entity.RestBean;
import c.e.entity.vo.request.ConfirmResetVO;
import c.e.entity.vo.request.EmailResetVo;
import c.e.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
import java.util.function.Supplier;

//处理验证相关信息
//让用户通过接口来请求邮件验证码
@Validated       //接口数据校验注解
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    @Resource
    AccountService service;
    //get请求用请求方式的参数接收
    @GetMapping("/ask-code") //发送邮件返回成功或失败，所以data信息就不需要
    public RestBean<Void> askVerifyCode(@RequestParam("email") @Email String email,  //用户邮箱参数
                                        @RequestParam("type") @Pattern(regexp = "(register|reset)") String type,  //判断是申请注册还是重置密码
                                        HttpServletRequest request){ //根据ip地址判断不要让用户重复请求
        return this.messageHandle(() -> service.registerEmailVerifyCode(type,email,request.getRemoteAddr()));
    }




    //修改密码 -- 验证验证码
    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo){
        return this.messageHandle(vo,service::resetConfirm);
    }

    //修改密码 -- 真正修改密码方法
    @PostMapping("/reset-password")
        public RestBean<Void> resetConfirm(@RequestBody @Valid EmailResetVo vo){
        return this.messageHandle(vo,service::resetEmailAccountPassword);
    }

    //简化代码
    private <T> RestBean messageHandle(T vo, Function<T,String> function){
        return messageHandle(() -> function.apply(vo));
    }

    //简化代码
    private RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null ? RestBean.success() : RestBean.failure(400,message);
    }

}
