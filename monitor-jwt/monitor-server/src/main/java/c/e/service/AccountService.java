package c.e.service;

import c.e.entity.dto.Account;
import c.e.entity.vo.request.ConfirmResetVO;
import c.e.entity.vo.request.EmailResetVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

//用户信息查询类  使用MyBatisPlus进行简化开发
public interface AccountService extends IService<Account>, UserDetailsService {

    //查询用户信息
    Account findAccountByNameOrEmail(String text);

    //注册邮件验证码
    String registerEmailVerifyCode(String type,String mail,String ip);


    //重置密码 -- 验证验证码是否正确
    String resetConfirm(ConfirmResetVO vo);

    //重置密码 -- 真正重置密码操作
    String resetEmailAccountPassword(EmailResetVo vo);
}
