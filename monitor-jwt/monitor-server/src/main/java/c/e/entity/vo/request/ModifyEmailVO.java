package c.e.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

//修改电子邮件的实体类
@Data
public class ModifyEmailVO {
    @Email
    String email;
    @Length(max = 6,min = 6)
    String code;
}
