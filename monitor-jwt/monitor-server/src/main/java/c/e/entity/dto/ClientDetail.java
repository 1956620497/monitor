package c.e.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("db_client_detail")
public class ClientDetail {

    //id,要跟客户端的ID相同
    @TableId
    Integer id;
    //操作系统架构
    String osArch;
    //操作系统名字
    String osName;
    //操作系统版本
    String osVersion;
    //操作系统位数
    int osBit;
    //CPU名字
    String cpuName;
    //CPU核心数
    int cpuCore;
    //内存容量
    double memory;
    //硬盘容量
    double disk;
    //ip地址
    String ip;

}
