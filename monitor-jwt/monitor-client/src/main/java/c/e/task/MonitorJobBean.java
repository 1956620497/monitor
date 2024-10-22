package c.e.task;

import c.e.entity.RuntimeDetail;
import c.e.utils.MonitorUtils;
import c.e.utils.NetUtils;
import jakarta.annotation.Resource;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;


//实现任务的任务类
@Component
public class MonitorJobBean extends QuartzJobBean {

    @Resource
    MonitorUtils monitor;

    @Resource
    NetUtils net;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //拿到运行时的数据
        RuntimeDetail runtimeDetail = monitor.monitorRuntimeDetail();
        //测试
//        System.out.println(runtimeDetail);
        net.updateRuntimeDetails(runtimeDetail);
    }

}
