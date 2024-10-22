package c.e.entity.vo.request;

import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientDetailVO {

    @NotNull
    //操作系统架构
    String osArch;
    @NotNull
    //操作系统名字
    String osName;
    @NotNull
    //操作系统版本
    String osVersion;
    @NotNull
    //操作系统位数
    int osBit;
    @NotNull
    //CPU名字
    String cpuName;
    @NotNull
    //CPU核心数
    int cpuCore;
    @NotNull
    //内存容量
    double memory;
    @NotNull
    //硬盘容量
    double disk;
    @NotNull
    //ip地址
    String ip;
}
