package c.e.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

//重置密码实体类
@Data
public class EmailResetVo {

    @Email
    String email;
    @Length(min = 6,max = 6)
    String code;
    @Length(min = 6,max = 20)
    String password;

}
