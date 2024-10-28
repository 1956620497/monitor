package c.e.entity.vo.request;


import lombok.Data;
import org.hibernate.validator.constraints.Length;


//用户更改密码实体类
@Data
public class ChangePasswordVO {

    //旧密码
    @Length(min=6,max = 20)
    String password;

    //新密码
    @Length(min=6,max = 20)
    String new_password;

}
