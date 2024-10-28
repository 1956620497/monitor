package c.e.entity.vo.response;

import com.alibaba.fastjson2.JSONArray;
import lombok.Data;

//查询子账户列表
@Data
public class SubAccountVO {
    int id;
    String username;
    String email;
    JSONArray clientList;
}
