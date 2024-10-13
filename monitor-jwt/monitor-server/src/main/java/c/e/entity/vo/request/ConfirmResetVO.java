package c.e.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

//重置密码  验证邮箱实体类
@Data
@AllArgsConstructor   //生成一个全参数构造函数      将这个类转换为对象
public class ConfirmResetVO {
    @Email
    String email;
    @Length(max = 6,min = 6)
    String code;
}
