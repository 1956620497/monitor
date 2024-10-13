package c.e.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

//配置类
@Data
@AllArgsConstructor
public class ConnectionConfig {
    //服务端地址
    String address;
    //token
    String token;
}
