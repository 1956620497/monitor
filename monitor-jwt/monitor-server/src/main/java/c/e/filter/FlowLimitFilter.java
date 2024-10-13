package c.e.filter;

import c.e.entity.RestBean;
import c.e.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


//进行限流操作
@Component
@Order(Const.ORDER_LIMIT)     //优先级
public class FlowLimitFilter extends HttpFilter {

    @Resource
    StringRedisTemplate template;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        //获取用户ip地址
        String address = request.getRemoteAddr();
        //如果成功计数
        if (this.tryCount(address)){
            //说明是正常的访问，就放行请求
            chain.doFilter(request,response);
        }else{
            //如果计数失败，就拒绝访问告诉用户请求太快了
            this.writeBlockMessage(response);
        }
    }

    //用户请求太快了，告诉用户不要请求太快了
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        //拒绝用户访问
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.forbidden("操作频繁，请稍后再试").asJsonString());
    }

    //限流操作：
    //希望用户一秒钟访问最多20次,方法是计数操作
    private boolean tryCount(String ip){
        //对ip地址加个锁，避免同时判断
        synchronized (ip.intern()){
            //用redis实现计数操作
            //查询一下redis中，用户ip有没有被封禁
            if(Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + ip)))
                //如果用户已经被封禁
                return false;
            //判断用户请求
            return this.limitPeriodCheck(ip);
        }
    }

    //积累用户请求数
    private boolean limitPeriodCheck(String ip){
        //判断有没有这个用户的计数键
        if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_COUNTER + ip))) {
            //如果有计数键，就让它去自增      有key刚好到期的情况，所以用Optional.ofNullable()方法设定如果为空就为0
            long increment = Optional.ofNullable(template.opsForValue().
                    increment(Const.FLOW_LIMIT_COUNTER + ip)).orElse(0L);
            //如果自增的请求大于10了,就封禁该ip 30秒
            if (increment > 10){
                template.opsForValue().set(Const.FLOW_LIMIT_BLOCK + ip,"",30,TimeUnit.SECONDS);
                return false;
            }
        }else{
            //如果没有计数键，就添加一个新的，该key只存在3秒钟，3秒后会重新设定一个新的
            template.opsForValue().set(Const.FLOW_LIMIT_COUNTER + ip,"1",3, TimeUnit.SECONDS);
        }
        //其他情况返回真
        return true;
    }

}
