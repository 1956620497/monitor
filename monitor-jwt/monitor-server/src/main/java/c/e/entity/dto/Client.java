package c.e.entity.dto;


import c.e.entity.BaseData;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

//客户端实体对象
@Data
@TableName("db_client")
@AllArgsConstructor
public class Client implements BaseData {

    //客户端id
    @TableId
    Integer id;
    //客户端名字
    String name;
    //客户端token
    String token;
    //地区
    String location;
    //节点
    String node;
    //注册时间
    Date registerTime;
}
