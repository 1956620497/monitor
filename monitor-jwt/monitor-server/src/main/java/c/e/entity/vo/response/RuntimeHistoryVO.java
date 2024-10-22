package c.e.entity.vo.response;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

//服务器历史数据
@Data
public class RuntimeHistoryVO {

    //磁盘总容量
    double disk;
    //内存总容量
    double memory;

    //运行时数据
    List<JSONObject> list = new LinkedList<>();

}
