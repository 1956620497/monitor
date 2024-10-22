package c.e.entity;

import lombok.Data;
import lombok.experimental.Accessors;

//运行时数据
//设备信息
@Data
@Accessors(chain = true)
public class RuntimeDetail {

    //时间戳
    long timestamp;
    //CPU使用率
    double cpuUsage;
    //内存使用率
    double memoryUsage;
    //磁盘使用率
    double diskUsage;
    //网络上传速度
    double networkUpload;
    //网络下载速度
    double networkDownload;
    //磁盘读取速度
    double diskRead;
    //磁盘的写入速度
    double diskWrite;






}
