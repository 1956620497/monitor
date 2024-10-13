package c.e.filter;

import c.e.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

//手动配置跨域
//用于实现拦截跨域请求的拦截器
@Component      //注册拦截器
@Order(Const.ORDER_CORS)    //拦截器的优先级
public class CorsFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        //向请求头中添加跨域信息
        this.addCorsHeader(request,response);
        //所有请求放行
        chain.doFilter(request,response);
    }

    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response){
        //"Access-Control-Allow-Origin"  这个响应头部指定了哪些来源可以访问该资源，必须与它匹配才能访问资源
        //Header中有一个请求是原始站点，Origin    getHeader("Origin")用于获取原始站点
        response.addHeader("Access-Control-Allow-Origin",request.getHeader("Origin"));
//        response.addHeader("Access-Control-Allow-Origin","http/localhost:5173");
        //Access-Control-Allow-Methods  规定了在跨域请求中允许使用的HTTP方法    允许的请求方式
        response.addHeader("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
        //Access-Control-Allow-Headers  规定了允许包含在请求中的HTTP头部字段    跨域请求只能携带这两个字段
        response.addHeader("Access-Control-Allow-Headers","Authorization,Content-Type");
    }

}
