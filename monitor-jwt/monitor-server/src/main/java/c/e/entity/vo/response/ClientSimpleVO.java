package c.e.entity.vo.response;


import lombok.Data;

//获取主机列表，只需要一些简单的数据
@Data
public class ClientSimpleVO {

    int id;
    String name;
    String location;
    String osName;
    String osVersion;
    String ip;

}
