package c.e.utils;

import c.e.entity.dto.RuntimeData;
import c.e.entity.vo.request.RuntimeDetailVO;
import c.e.entity.vo.response.RuntimeHistoryVO;
import com.alibaba.fastjson2.JSONObject;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

//InfluxDB数据库的工具类
@Component
public class InfluxDbUtils {

    //取出配置信息
    @Value("${spring.influx.url}")
    String url;
    @Value("${spring.influx.user}")
    String user;
    @Value("${spring.influx.password}")
    String password;

    private final String BUCKET = "monitor";
    private final String ORG = "Luna";

    //引入InfluxDB对象
    private InfluxDBClient client;

    //构建客户端
    @PostConstruct
    public void init(){
        client = InfluxDBClientFactory.create(url,user,password.toCharArray());
    }

    //存储数据
    public void writeRuntimeData(int clientId, RuntimeDetailVO vo){
        //打包实体类
        RuntimeData data = new RuntimeData();
        BeanUtils.copyProperties(vo,data);
        //因为时间戳单位不同，需要转换一下
        data.setTimestamp(new Date(vo.getTimestamp()).toInstant());
        data.setClientId(clientId);

        //getWriteApiBlocking()提供同步的方式将数据写入数据库
        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        //第一个参数是数据库的名字，第二个参数是用户的名字
        writeApi.writeMeasurement(BUCKET,ORG, WritePrecision.NS,data);
    }

    //取出数据
    public RuntimeHistoryVO readRuntimeData(int clientId){
        //要存储的vo对象
        RuntimeHistoryVO vo = new RuntimeHistoryVO();
        //查询语句
        String query = """
                from(bucket: "%s")
                |> range(start: %s)
                |> filter(fn: (r) => r["_measurement"] == "runtime")
                |> filter(fn: (r) => r["clientId"] == "%s")
                """;
        //-1表示过去一个小时
        String format = String.format(query,BUCKET,"-1h",clientId);
        //查询操作  tables里面是，每一个field对应一个value
        List<FluxTable> tables = client.getQueryApi().query(format, ORG);
        //查询
        int size = tables.size();
        //有可能一开始的时候数据没有
        if (size == 0) return vo;
        //从第0张表查询出所有的总条目
        List<FluxRecord> records = tables.get(0).getRecords();
        for (int i = 0 ; i < records.size() ; i++){
            JSONObject object = new JSONObject();
            object.put("timestamp",records.get(i).getTime());
            for (int j = 0 ; j < size ; j++){
                FluxRecord record = tables.get(j).getRecords().get(i);
                object.put(record.getField(),record.getValue());
            }
            vo.getList().add(object);
        }
        return vo;
    }
}
