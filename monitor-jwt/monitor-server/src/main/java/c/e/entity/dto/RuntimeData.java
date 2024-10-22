package c.e.entity.dto;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;


//向InfluxDB数据库存储的实体类
@Data
@Measurement(name = "runtime")   //表的名字,不是数据库的名字
public class RuntimeData {

    //将id定义为tag
    @Column(tag = true)
    //客户端ID
    int clientId;

    //时间戳        将时间戳设置为true，以时间戳来存储数据    注意数据类型是Instant类型，InfluxDB好像只支持这个
    @Column(timestamp = true)
    Instant timestamp;

    //CPU使用率
    @Column
    double cpuUsage;
    //内存使用率
    @Column
    double memoryUsage;
    //磁盘使用率
    @Column
    double diskUsage;
    //网络上传速度
    @Column
    double networkUpload;
    //网络下载速度
    @Column
    double networkDownload;
    //磁盘读取速度
    @Column
    double diskRead;
    //磁盘写入速度
    @Column
    double diskWrite;

}
