package c.e.entity.dto;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

//客户端实体对象
@Data
@TableName("db_client")
@AllArgsConstructor
public class Client {
    @TableId
    Integer id;
    String name;
    String token;
    Date registerTime;
}
