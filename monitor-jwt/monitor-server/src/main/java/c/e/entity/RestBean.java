package c.e.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public record RestBean<T>(int code, T data, String message) {
    //成功方法
    public static <T> RestBean<T> success(T data){
        return new RestBean<>(200,data,"请求成功");
    }

    //成功方法
    public static <T> RestBean<T> success(){
        return success(null);
    }

    //未登录的情况
    public static <T> RestBean<T> unauthorized(String message){
        return failure(401,message);
    }

    //登录了权限不足的情况
    public static <T> RestBean<T> forbidden(String message){
        return failure(401,message);
    }

    //失败方法
    public static <T> RestBean<T> failure(int code,String message){
        return new RestBean<>(code,null,message);
    }

    //变成JSON格式
    public String asJsonString(){
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
