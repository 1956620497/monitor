package c.e.entity.vo.response;

import lombok.Data;

@Data
public class ClientDetailsVO {

    //主机id
    int id;
    //主机名字
    String name;
    //主机是否运行
    boolean online;
    //主机节点
    String node;
    //主机位置
    String location;
    //ip地址
    String ip;
    //cpu名字
    String cpuName;
    //操作系统名字
    String osName;
    //操作系统版本
    String osVersion;
    //内存容量
    double memory;
    //cpu核心数
    int cpuCore;
    //存储空间
    double disk;


}
