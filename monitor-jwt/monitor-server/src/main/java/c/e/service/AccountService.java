package c.e.service;

import c.e.entity.dto.Account;
import c.e.entity.vo.request.ConfirmResetVO;
import c.e.entity.vo.request.CreateSubAccountVO;
import c.e.entity.vo.request.EmailResetVo;
import c.e.entity.vo.request.ModifyEmailVO;
import c.e.entity.vo.response.SubAccountVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

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

    //修改密码
    boolean changePassword(int id,String oldPass,String newPass);

    //创建子账户
    String createSubAccount(CreateSubAccountVO vo);

    //删除子账户
    void deleteSubAccount(int uid);

    //查询子账户
    List<SubAccountVO> listSubAccount();

    //修改邮件验证码
    String modifyEmail(int id, ModifyEmailVO vo);

}
