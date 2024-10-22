package c.e.entity.vo.response;

import lombok.Data;

@Data
public class ClientPreviewVO {

    //服务器id
    int id;
    //服务器是否运行状态
    boolean online;
    //服务器名字
    String name;
    //服务器地区
    String location;
    //操作系统名字
    String osName;
    //操作系统版本
    String osVersion;
    //服务器的公网IP
    String ip;
    //服务器的CPU名字
    String cpuName;
    //服务器的CPU核心数
    int cpuCore;
    //总内存
    double memory;
    //CPU使用率
    double cpuUsage;
    //内存使用率
    double memoryUsage;
    //网络上传速度
    double networkUpload;
    //网络下载速度
    double networkDownload;
}
