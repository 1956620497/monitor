package c.e.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//配置JWT
@Component
public class JWTUtils {

    //加密密钥
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;


    //创建JWT令牌
    //直接从User中读取，拿到的是哪一个用户
    //UserDetails用来读取用户信息
    public String createJwt(UserDetails details,int id,String username){
        //加密算法
        Algorithm algorithm = Algorithm.HMAC256(key);
        //过期时间
        Date expire = this.expireTime();
        //返回JWT数据
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())    //给令牌设置一个id，方便用户登出时将令牌拉黑
                .withClaim("id",id)
                .withClaim("name",username)
                .withClaim("authorities",details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())    //权限
                .withExpiresAt(expire)         //过期时间
                .withIssuedAt(new Date())    //token颁发时间
                .sign(algorithm);          //签名

    }


    //设置一下过期时间
    public Date expireTime(){
        //calendar表示返回当前日期的一个实例
        Calendar calendar = Calendar.getInstance();
        //计算过期时间，使用add方法增加过期时间
        calendar.add(Calendar.HOUR,expire * 24);
        //返回时间并转化为Date对象
        return calendar.getTime();
    }

    //判断Jwt是否合法
    public DecodedJWT resolveJwt(String headerToken){
        //判断是否合法，判断token头是否包含Bearer
        String token = this.convertToken(headerToken);
        //如果返回的验证结果为空，直接返回空值
        if (token == null) return null;
        //用生成token同样的加密方法,同样的key来解析
        Algorithm algorithm = Algorithm.HMAC256(key);
        //解析方法
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try{
            //验证当前的JWT是否合法,是否被用户篡改过,如果被篡改过，这里会抛一个验证异常
            DecodedJWT verify = jwtVerifier.verify(token);
            //判断JWT是否被拉黑
            if (this.isInvalidToken(verify.getId()))
                return null;

            //判断当前用户有没有被拉黑，与上面的区别是，上面是判断token是否被拉黑，这里判断用户是否被拉黑
            if (this.isInvalidUser(verify.getClaim("id").asInt()))
                return null;
//            Map<String,Claim> claims = verify.getClaims();
//            return new Date().after(claims.get("exp").asDate()) ? null : verify;

            //查看令牌是否过期
            //首先获取令牌过去的日期
            Date expiresAt = verify.getExpiresAt();
            //判断一下现在是否已经超过了过期的日期
            //如果今天已经超过了过期的日期，同样也返回空，如果没超过，就把解析后的jwt返回回去
            return new Date().after(expiresAt) ? null : verify;

            /*
            运行时异常，不会显式的抛出，必须自己去捕获
            如果运行时异常，说明JWT有问题，所以直接返回空
             */
        }catch (JWTVerificationException e){
            return null;
        }
    }

    //判断一下token是否合法
    private String convertToken(String headerToken){
        //如果token为空或者前面的字符不为Bearer时，直接返回空
        if (headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        //如果通过上面验证了，直接返回token，但是从第七个字符开始切割，因为前7个字符是Bearer
        return headerToken.substring(7);
    }

    //解析JWT中的用户信息      把jwt中的信息变成UserDetails
    public UserDetails toUser(DecodedJWT jwt){
        //从解码的JWT中获取信息的键值对
        Map<String, Claim> claims = jwt.getClaims();
        //用User类的withUsername()方法创建一个用户对象，并将用户名、密码、权限设置为获取到的数据
        return User
                .withUsername(claims.get("name").asString())  //claims.get("name").asString()获取用户名
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))   //用户角色权限之类的信息都在这个里面
                .build(); //构建UserDetails对象
    }

    //解析成id
    public Integer toId(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    //让令牌失效的方法
    public boolean invalidateJwt(String headerToken){
        //判断是否合法，判断token头是否包含Bearer
        String token = this.convertToken(headerToken);
        //如果返回的验证结果为空，直接返回空值
        if (token == null) return false;
        //用生成token同样的加密方法,同样的key来解析
        Algorithm algorithm = Algorithm.HMAC256(key);
        //解析方法
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try{
            //验证当前jwt是否合法，是否被用户篡改过，如果被用户篡改过会报一个运行时异常
            DecodedJWT jwt = jwtVerifier.verify(token);
            //获取到创建JWT时生成的id
            String id = jwt.getId();
            //将token的id添加到黑名单后返回结果
            return deleteToken(id,jwt.getExpiresAt());
        }catch (JWTVerificationException e){
            return false;
        }

    }

    //将token拉入黑名单
    /**
     * 将token拉入redis黑名单中
     * @param uuid  令牌ID
     * @param time  过期时间
     * @return  是否操作成功
     */
    public boolean deleteToken(String uuid,Date time){

        //判断token是否失效
        if (this.isInvalidToken(uuid))
            return false;
        //获取当前时间
        Date now = new Date();
        //计算剩余时间
        long expire = Math.max(time.getTime() - now.getTime(),0); //时间默认是毫秒
        //将token存入黑名单中     第四个参数表示过期时间是毫秒   第三个参数表示设置的过期时间
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid,"",expire, TimeUnit.MILLISECONDS);
        //返回添加黑名单成功
        return true;
    }

    //将用户token拉入黑名单
    public void deleteUser(int uid){
        template.opsForValue().set(Const.USER_BLACK_LIST + uid,"",expire,TimeUnit.HOURS);
    }

    //查询是否是无效用户
    public boolean isInvalidUser(int uid){
        return Boolean.TRUE.equals(template.hasKey(Const.USER_BLACK_LIST + uid));
    }

    //判断令牌是否过期失效
    /**
     * 验证token是否被列入Redis黑名单
     * @param uuid  令牌ID
     * @return  是否操作成功
     */
    public boolean isInvalidToken(String uuid){
        //在redis的黑名单中查找有没有这个令牌
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }

}
