package c.e.entity.dto;

import c.e.entity.BaseData;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

//ssh连接数据实体对象
@Data
@TableName("db_client_ssh")
public class ClientSsh implements BaseData {

    @TableId
    Integer id;
    Integer port;
    String username;
    String password;
    String ip;

}
