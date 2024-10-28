package c.e.entity.vo.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

//子账户实体类
@Data
public class CreateSubAccountVO {

    //用户名
    @Length(min = 1,max = 10)
    String username;
    //邮箱
    @Email
    String email;
    //密码
    @Length(min = 6,max = 20)
    String password;
    //允许管理哪些客户端  至少要管理一个服务器
    @Size(min = 1)
    List<Integer> clients;

}
