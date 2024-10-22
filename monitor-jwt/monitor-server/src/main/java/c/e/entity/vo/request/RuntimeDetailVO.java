package c.e.entity.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RuntimeDetailVO {

    //时间戳
    @NotNull
    long timestamp;
    //CPU使用率
    @NotNull
    double cpuUsage;
    //内存使用率
    @NotNull
    double memoryUsage;
    //磁盘使用率
    @NotNull
    double diskUsage;
    //网络上传速度
    @NotNull
    double networkUpload;
    //网络下载速度
    @NotNull
    double networkDownload;
    //磁盘读取速度
    @NotNull
    double diskRead;
    //磁盘写入速度
    @NotNull
    double diskWrite;

}
