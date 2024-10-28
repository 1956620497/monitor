package c.e.entity.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RenameClientVO {

    @NotNull
    int id;
    @Length(min = 1,max = 15)
    String name;

}
