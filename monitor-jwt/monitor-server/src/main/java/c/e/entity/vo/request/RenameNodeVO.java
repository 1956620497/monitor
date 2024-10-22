package c.e.entity.vo.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


//节点信息
@Data
public class RenameNodeVO {

    //服务器id
    int id;
    //节点名称
    @Length(min = 1,max = 10)
    String node;
    //地区，用于更改图标
    @Pattern(regexp = "(cn|hk|jp|us|sg|kr|de)")
    String location;

}
