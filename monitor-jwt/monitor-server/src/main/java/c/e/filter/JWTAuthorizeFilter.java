package c.e.filter;

import c.e.entity.RestBean;
import c.e.entity.dto.Account;
import c.e.entity.dto.Client;
import c.e.service.AccountService;
import c.e.service.ClientService;
import c.e.utils.Const;
import c.e.utils.JWTUtils;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jogamp.nativewindow.windows.AccentPolicy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

//验证token

/**
 * 由于使用了SpringSecurity框架，所以验证也要跟着安全框架走，它内部有一套过滤器链，有一套自己的校验机制
 * 所以要把自己写的过滤器链添加到它的里面去
 */

//每次请求都会执行一次的过滤器OncePerRequestFilter
@Component
public class JWTAuthorizeFilter extends OncePerRequestFilter {

    @Resource
    JWTUtils utils;

    @Resource
    ClientService service;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //从请求头里面读取token
        String authorization  = request.getHeader("Authorization");

        //获取走的是哪一个接口
        String uri = request.getRequestURI();
        //判断是否走的是指定接口
        if (uri.startsWith("/monitor")){
            //是的话就一律不管
            //看一下请求是注册还是访问，如果是访问的话，直接将参数塞到attribute中
            if (!uri.endsWith("/register")){
                //如果不是注册接口，直接塞数据进去
                Client client = service.findClientByToken(authorization);
                //如果没有，说明没注册过
                if (client == null){
                    //没注册过，不允许访问接口
                    response.setStatus(401);
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(RestBean.failure(401,"未注册").asJsonString());
                    return;
                }else{
                    //将客户端实体对象塞到Attribute中
                    request.setAttribute(Const.ATTR_CLIENT,client);
                }
            }
        }else{
            //不是的话就按照之前的流程继续走

            //将token在工具类中判断是否合法并且截取
            DecodedJWT jwt = utils.resolveJwt(authorization);
            //判断一下
            if (jwt != null){
                //如果不等于空,把jwt中存入的信息解析出来
                UserDetails user = utils.toUser(jwt);
                //手动授权     UsernamePasswordAuthenticationToken是Security内部的一个验证token的方法  要把用户的信息给进去
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
                //将web请求的详细信息附加到正在进行的用户身份验证中
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //将一个经过身份验证的用户认证对象设置到security的安全上下文中，以便在整个应用程序范围内访问和使用这个身份验证信息。
                SecurityContextHolder.getContext().setAuthentication(authentication);
                //将用户信息存到公共域对象中，为了后续更加方便的取用，在后续可以用request.getAttribute("id")来去出值
                request.setAttribute(Const.ATTR_USER_ID,utils.toId(jwt));
                //将用户角色读取出来，然后再塞进去
                request.setAttribute(Const.ATTR_USER_ROLE,
                        new ArrayList<>(user.getAuthorities()).get(0).getAuthority());

                //验证建立SSH连接的请求有没有权限
                //这里写的感觉怪怪的，判断请求地址开头是否是ssh连接的地址，并且没有权限
                //感觉这样有些不妥，后面再重新做
                if (request.getRequestURI().startsWith("/terminal/") && !accessShell(
                        (Integer) request.getAttribute(Const.ATTR_USER_ID),
                        (String) request.getAttribute(Const.ATTR_USER_ROLE),
                        Integer.parseInt(request.getRequestURI().substring(10)))){
                    //返回错误信息
                    response.setStatus(401);
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(RestBean.failure(401,"无权访问").asJsonString());
                    return;
                }
            }

        }
        //如果等于空
        filterChain.doFilter(request,response);
    }

    @Resource
    AccountService accountService;

    private boolean accessShell(int userId,String userRole,int clientId){
        //如果是管理员账户，直接返回true
        if (Const.ROLE_ADMIN.equals(userRole.substring(5))){
            return true;
        }else {
            //如果是子用户，查询用户信息，看看允许访问的虚拟机列表中是否包含
            Account account = accountService.getById(userId);
            return account.getClientList().contains(clientId);
        }
    }

}
