package c.e.entity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

//服务端响应实体类
public record Response(int id,int code,Object data,String message) {

    public boolean success(){
        return code == 200;
    }

    //转换json格式
    public JSONObject asJson(){
        return JSONObject.from(data);
    }

    //转换String类型
    public String asString(){
        return data.toString();
    }

    //封一个出问题的报错的方法
    public static Response errorResponse(Exception e){
        return new Response(0,500,null,e.getMessage());
    }
}
