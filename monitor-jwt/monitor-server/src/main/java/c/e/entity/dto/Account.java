package c.e.entity.dto;

import c.e.entity.BaseData;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.Date;
import java.util.List;

//用户登录的实体类
@Data
@TableName("db_account")     //MyBatisPlus相关注解
@AllArgsConstructor
public class Account implements BaseData {

    @TableId(type = IdType.AUTO)

    //element_plus推荐使用包装类
    Integer id;   //用户id
    String username; //用户名   唯一
    String password; //密码
    String email; //邮箱  唯一
    String role; //角色，权限
    Date registerTime; //用户注册时间
    String clients; //允许管理哪些服务器

//    Date lastTime; //用户最后一次登录时间

    //快速将本对象转换为List
    public List<Integer> getClientList(){
        //Collections.emptyList();返回一个不可修改的空列表
        if (clients == null) return Collections.emptyList();
        return JSONArray.parse(clients).toList(Integer.class);
    }

}
