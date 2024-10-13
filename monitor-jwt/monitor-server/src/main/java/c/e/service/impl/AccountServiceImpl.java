package c.e.service.impl;

import c.e.entity.dto.Account;
import c.e.entity.vo.request.ConfirmResetVO;
import c.e.entity.vo.request.EmailResetVo;
import c.e.mapper.AccountMapper;
import c.e.service.AccountService;
import c.e.utils.Const;
import c.e.utils.FlowUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    //引入rabbitmq
    @Resource
    AmqpTemplate amqpTemplate;

    //引入Redis
    @Resource
    StringRedisTemplate stringRedisTemplate;

    //引入限流工具类
    @Resource
    FlowUtils utils;

    //引入加密工具
    @Resource
    PasswordEncoder encoder;


    //登录时查询用户信息
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        //如果查询到了，转换为user对象传回去
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    //发送邮箱验证码
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        //加线程锁，防止同一请求被多次调用     synchronized (ip.intern()) 同一个ip发送过来的请求一定会排队
        synchronized (ip.intern()){
            //判断用户有没有重复发送
            if (!this.verifyLimit(ip))
                //如果重复发送了
                return "请求频繁！请稍后再试";

            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            //将用户数据封装起来
            Map<String,Object> data = Map.of("type",type,"email",email,"code",code);
            //使用异步，将数据放进队列  Rabbitmq
            amqpTemplate.convertAndSend("mail",data);
            //发送完邮件还不行，得要在redis中存入验证码,并设置三分钟有效
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }
    }

    //查询用户信息
    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();
    }

    //限制用户发送
    private boolean verifyLimit(String ip){
        //生成用户key用来限制用户重复点击发送
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        //写完后记得将过期时间抽取到配置文件中
        return utils.limitOnceCheck(key,60);
    }





    //重置密码 第一层验证-- 验证邮箱验证码是否正确
    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        //取出请求中的邮箱
        String email = vo.getEmail();
        //取出缓存中的验证码
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        //如果缓存中验证码为空，说明用户没有获取
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        return null;
    }

    //重置密码 第二层修改-- 真正重置密码操作
    @Override
    public String resetEmailAccountPassword(EmailResetVo vo) {
        String email  = vo.getEmail();
        //验证一下验证码是否正确
        String verify = this.resetConfirm(new ConfirmResetVO(email,vo.getCode()));
        //如果没有为空，说明验证码不对
        if (verify != null) return verify;
        //如果为空，说明验证码是对的
        String password = encoder.encode(vo.getPassword());
        //更新密码
        boolean update = this.update().eq("email",email).set("password",password).update();
        if (update){
            //更新成功
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;
    }
}
