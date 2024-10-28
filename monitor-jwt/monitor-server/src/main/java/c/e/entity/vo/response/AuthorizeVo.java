package c.e.entity.vo.response;

import c.e.entity.BaseData;
import lombok.Data;

import java.util.Date;

//响应VO
//用户详细信息
@Data
public class AuthorizeVo implements BaseData {

    String username;
    String email;
    String role;
    String token;
    Date expire;

}
