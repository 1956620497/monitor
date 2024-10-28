package c.e.entity.vo.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

//返回ssh连接信息
@Data
public class SshSettingsVO {

    String ip;
    int port = 22;
    String username;
    String password;

}

