package c.e.utils;

import c.e.entity.BaseDatail;
import c.e.entity.RuntimeDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

//读取硬件信息的工具类
@Slf4j
@Component
public class MonitorUtils {

    //oshi框架，通过它来获取操作系统的信息
    private final SystemInfo info = new SystemInfo();
    //JDK提供的读取操作系统相关的属性，但是不够详细
    private final Properties properties = System.getProperties();


    //基本信息上报
    public BaseDatail monitorBaseDetail(){
        //info中的方法getOperatingSystem()是获取操作系统信息，是属于软件一类的信息，getHardware()是获取硬件信息
        //获取软件信息
        OperatingSystem os = info.getOperatingSystem();
        //获取硬件信息
        HardwareAbstractionLayer hardware = info.getHardware();
        //计算内存容量,获取的默认单位是字节，转换成GB返回回去
        double memory = hardware.getMemory().getTotal() / 1024.0 / 1024 / 1024;
        //硬盘容量  将每一块硬盘的容量相加       然后将单位转换成GB
        double diskSize = Arrays.stream(File.listRoots())
                .mapToLong(File::getTotalSpace).sum() / 1024.0 / 1024 /1024;
        //拿到本地ip地址     Objects.requireNonNull()检查是否返回值为空，getIPv4addr()[0];获取第一个ip地址
        String ip = Objects.requireNonNull(this.findNetworkInterface(hardware)).getIPv4addr()[0];
        return new BaseDatail()
                //操作系统
                .setOsArch(properties.getProperty("os.arch"))
                //操作系统名字
                .setOsName(os.getFamily())
                //操作系统版本
                .setOsVersion(os.getVersionInfo().getVersion())
                //操作系统位数
                .setOsBit(os.getBitness())
                //cpu名字
                .setCpuName(hardware.getProcessor().getProcessorIdentifier().getName())
                //逻辑处理器核心
                .setCpuCore(hardware.getProcessor().getLogicalProcessorCount())
                //内存容量
                .setMemory(memory)
                //硬盘容量
                .setDisk(diskSize)
                //获取ip地址
                .setIp(ip);
    }

    //运行时数据上传
    public RuntimeDetail monitorRuntimeDetail(){
        //oshi框架对于设备运行时的数据，采用的是统计一段时间的设备数据，然后取平均值返回
        //不支持获取某一个时刻的设备使用情况
        //设定一个分析的时间,即统计时间
        //单位:秒
        double statisticTime = 1;
        try{
            //获取oshi
            HardwareAbstractionLayer hardware = info.getHardware();
            //拿到网络数据，因为要监测读写
            NetworkIF networkInterface = Objects.requireNonNull(this.findNetworkInterface(hardware));

            //换一种方式拿到网卡
//            NetworkIF networkInterface = Objects.requireNonNull(this.newNetwork(hardware));

            //获取CPU运行时信息
            CentralProcessor processor = hardware.getProcessor();
            //读取网络上传多少字节的数据     getBytesSent()返回发送了多少字节的数据
            double upload = networkInterface.getBytesSent();
            //读取网络下载多少字节的数据    getBytesRecv()返回下载了多少字节的数据
            double download = networkInterface.getBytesRecv();
            //获取硬盘读取速度       getDiskStores()获取所有硬盘  将所有硬盘的读写量都集中到一起
            //先记录一次硬盘已写入量，然后等待设定的监测时间，然后再获取一次硬盘的写入数据量
            //用第二次获取的数据量减去第一次的数据，就获取到了最终的速度
            double read = hardware.getDiskStores().stream().mapToLong(HWDiskStore::getReadBytes).sum();
            //硬盘写入速度
            double write = hardware.getDiskStores().stream().mapToLong(HWDiskStore::getWriteBytes).sum();
            //获取CPU的各项指标，这些数据是保存在数组中的    需要根据时间进行计算
            long[] ticks = processor.getSystemCpuLoadTicks();


            //计算网络
//            double upload1 = this.newNetwork(hardware).getBytesSent();
//            double download1 = this.newNetwork(hardware).getBytesRecv();

            //获取初始的CPU tick数  新
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            //设置休眠线程时间       Thread.sleep() 需要以毫秒为单位
            Thread.sleep((long)(statisticTime * 1000));
            //暂停时间后再次获取CPU tick数  新
            long[] ticks2 = processor.getSystemCpuLoadTicks();

            //计算网络
//            double upload2 = this.newNetwork(hardware).getBytesSent();
//            double download2 = this.newNetwork(hardware).getBytesRecv();

            //计算
//            double download3 = (download2 - download1) / statisticTime;
//            double upload3 = (upload2 - upload1) / statisticTime;

//            System.err.println("下载速度:" + download3);
//            System.err.println("上传速度:" + upload3);

            //开始计算
            //然后需要获取一次最新的网卡状态
            networkInterface = Objects.requireNonNull(this.findNetworkInterface(hardware));
            //上传速度,单位字节每秒    用最新的数据量，减去之前的数据量,除以统计时间，最终的结果就是下载速度
            upload = (networkInterface.getBytesSent() - upload) / statisticTime;
            //下载速度，单位字节每秒
            download = (networkInterface.getBytesRecv() - download) / statisticTime;
            //硬盘读取速度
            read = (hardware.getDiskStores().stream().mapToLong(HWDiskStore::getReadBytes).sum() - read)
                    / statisticTime;
            //硬盘写入速度
            write = (hardware.getDiskStores().stream().mapToLong(HWDiskStore::getWriteBytes).sum() -write)
                    / statisticTime;
            //内存使用量     将内存总量减去可用内存数量    默认单位是字节，需要转换为GB
            double memory = (hardware.getMemory().getTotal() - hardware.getMemory().getAvailable())
                    / 1024.0 / 1024 / 1024;
            //磁盘使用量     File.listRoots()
            double disk = Arrays.stream(File.listRoots())
                    //计算剩余空间     总容量-空闲容量=已用掉的容量
                    .mapToLong(file -> file.getTotalSpace() - file.getFreeSpace()).sum() / 1024.0 / 1024 / 1024;
            return new RuntimeDetail()
                    .setCpuUsage(this.calculateCpuUsage(processor,ticks))
                    //换一种方式计算CPU的使用率
//                    .setCpuUsage(cpuUsage(prevTicks,ticks2))
                    .setMemoryUsage(memory)
                    .setDiskUsage(disk)
                    .setNetworkUpload(upload / 1024)  //单位KB
                    .setNetworkDownload(download / 1024)  //单位KB

//                    .setNetworkUpload(upload3)
//                    .setNetworkDownload(download3)

                    .setDiskRead(read / 1024 / 1024 )    //单位MB
                    .setDiskWrite(write / 1024 / 1024 )   //单位MB
                    .setTimestamp(new Date().getTime());
        }catch (Exception e){
            log.error("读取运行时数据出现问题",e);
        }
        return null;
    }

    //运行时CPU信息
    /**
     * @param processor
     * @param prevTicks CPU各项数据指标
     * @return  当前CPU使用情况
     */
    private double calculateCpuUsage(CentralProcessor processor,long[] prevTicks){
        //获取CPU的各项指标
        long[] ticks = processor.getSystemCpuLoadTicks();
        //计算各项指标
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
                - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softIrq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
                - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
                - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long cUser = ticks[CentralProcessor.TickType.USER.getIndex()]
                - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
                - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
                - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = cUser + nice + cSys + idle + ioWait + irq + softIrq + steal;
        //(系统使用量+用户使用量) / 总使用量 = 当前CPU占用情况
        return (cSys + cUser) * 1.0 / totalCpu;
//        return (totalCpu - idle) * 1.0 / totalCpu;
    }

    //换一种CPU使用率计算方式
    public double cpuUsage(long[] prevTicks,long[] ticks){
        long totalCpu = 0;
        for (int i = 0; i < CentralProcessor.TickType.values().length; i++) {
            totalCpu += ticks[i] - prevTicks[i];
        }
        double cpuUsage = (double) (totalCpu - (ticks[CentralProcessor.TickType.IDLE.getIndex()] -
                prevTicks[CentralProcessor.TickType.IDLE.getIndex()])) / totalCpu;
        log.error(String.valueOf(cpuUsage * 100));
        return cpuUsage * 100;
    }



    //获取ip
    /**
     * 获取当前正在使用的网卡，可能有多块网卡，这里就获取当前正在使用的网卡
     * @param hardware  获取当前系统中的所有信息
     * @return  本地IP地址  有ip地址的网卡
     */
    private NetworkIF findNetworkInterface(HardwareAbstractionLayer hardware){
        try{
            for (NetworkIF network : hardware.getNetworkIFs()){
                String[] ipv4Addr = network.getIPv4addr();
                NetworkInterface ni = network.queryNetworkInterface();
                //!ni.isLoopback()不能是回环接口
                //!ni.isPointToPoint()不能是p2p点对点接口
                //ni.isUp()网卡处于启用状态
                //!ni.isVirtual()不是虚拟网卡
                //接口名称以eth或者en开头
                //并且至少有一个IPv4地址
                if (!ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                        && (ni.getName().startsWith("eth") || ni.getName().startsWith("en") || ni.getName().startsWith("Ethernet"))
                        && ipv4Addr.length > 0){
//                if (ni.getName().startsWith("wlan0") && ipv4Addr.length > 0){

                    return network;
                }
            }
        }catch (Exception e){
            log.error("读取网络接口信息时出错",e);
        }
        return null;
    }


    //换一种方法获取网卡
    public NetworkIF newNetwork(HardwareAbstractionLayer hardware) throws SocketException {
        for (NetworkIF net : hardware.getNetworkIFs()){
            NetworkInterface ni = net.queryNetworkInterface();
            // 排除本地回环网卡
            if (!ni.isLoopback() && (net.getBytesRecv() > 0 || net.getBytesSent() > 0)) {
                System.out.println("正在使用的网卡: " + net.getDisplayName());
                String ip = Objects.requireNonNull(net).getIPv4addr()[0];
                System.out.println("正在使用的ip" + ip);
                return net;
            }
        }
        return null;
    }

}
