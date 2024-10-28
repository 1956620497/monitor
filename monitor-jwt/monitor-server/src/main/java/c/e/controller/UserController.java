package c.e.controller;

import c.e.entity.RestBean;
import c.e.entity.vo.request.ChangePasswordVO;
import c.e.entity.vo.request.CreateSubAccountVO;
import c.e.entity.vo.request.ModifyEmailVO;
import c.e.entity.vo.response.SubAccountVO;
import c.e.service.AccountService;
import c.e.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    AccountService service;

    //更改密码
    @PostMapping("/change-password")
    public RestBean<Void> changePassword(@RequestBody @Valid ChangePasswordVO vo,
                                         @RequestAttribute(Const.ATTR_USER_ID) int userId){
        return service.changePassword(userId,vo.getPassword(), vo.getNew_password()) ?
                RestBean.success() : RestBean.failure(401,"原密码输入错误!");
    }

    //修改电子邮件
    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(@RequestAttribute(Const.ATTR_USER_ID)int id,
                                      @RequestBody @Valid ModifyEmailVO vo){
        String result = service.modifyEmail(id, vo);
        if (result == null ){
            return RestBean.success();
        } else {
            return RestBean.failure(401,result);
        }
    }

    //创建子用户
    @PostMapping("/sub/create")
    public RestBean<Void> createSubAccount(@RequestBody @Valid CreateSubAccountVO vo){
//        service.createSubAccount(vo);
//        return RestBean.success();
        String sub = service.createSubAccount(vo);
        return sub == null ? RestBean.success() :
                RestBean.failure(401,sub);
    }

    //删除子账户
    @GetMapping("/sub/delete")
    public RestBean<Void> deleteSubAccount(int uid,
                                           @RequestAttribute(Const.ATTR_USER_ID)int userId){
        //校验一下，不能让自己删除自己
        if (uid == userId)
            return RestBean.failure(401,"非法参数");
        //执行删除
        service.deleteSubAccount(uid);
        return RestBean.success();
    }

    //查询子账户列表
    @GetMapping("/sub/list")
    public RestBean<List<SubAccountVO>> subAccountList(){
        return RestBean.success(service.listSubAccount());
    }

}
