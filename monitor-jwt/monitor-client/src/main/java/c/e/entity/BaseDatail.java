package c.e.entity;

import lombok.Data;
import lombok.experimental.Accessors;

//基本信息
//设备信息实体类
@Data
@Accessors(chain = true)  //允许set方法的链式调用，被调用时生成的set方法会返回当前对象this，而不是void
public class BaseDatail {

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
