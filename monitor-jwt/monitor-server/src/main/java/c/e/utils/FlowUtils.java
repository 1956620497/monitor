package c.e.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

//使用redis制作的限流工具
@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate template;

    //限制用户在三分钟之内不能发送邮件
    public boolean limitOnceCheck(String key,int blockTime){
        if (Boolean.TRUE.equals(template.hasKey(key))){
            //正在冷却的状态
            return false;
        }else{
            //不在冷却的状态    向redis中输入一个值，设置过期时间为60秒
            //这里向redis存入一个用户的键，如果键存在表示正在冷却
            template.opsForValue().set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }
    }

}
