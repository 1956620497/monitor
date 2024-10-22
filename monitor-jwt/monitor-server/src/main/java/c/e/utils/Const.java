package c.e.utils;

//用这个类来存储经常会用到的属性
public class Const {
    //用于在redis中存储用户token的前缀
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";

    public final static String JWT_FREQUENCY = "jwt:frequency:";

    //用于在redis中存储用户注册时邮箱的验证码
    public static final String VERIFY_EMAIL_DATA = "verify:email:data:";

    //用于防止用户重复发送邮件
    public static final String VERIFY_EMAIL_LIMIT = "verify:email:limit:";


    //跨域过滤器的优先级
    public static final int ORDER_CORS = -102;

    //限流操作过滤器，优先级要比跨域优先级低一点
    public static final int ORDER_LIMIT = -101;

    //限流操作 用户的ip的计数器
    public static final String FLOW_LIMIT_COUNTER = "flow:counter:";

    //限流操作 被封禁的用户ip前缀
    public static final String FLOW_LIMIT_BLOCK = "flow:block:";

    //监控系统客户端
    public static final String ATTR_CLIENT = "client";

    //请求自定义属性
    public final static String ATTR_USER_ID = "userId";

    //用户角色
    public final static String ROLE_DEFAULT = "admin";


}
