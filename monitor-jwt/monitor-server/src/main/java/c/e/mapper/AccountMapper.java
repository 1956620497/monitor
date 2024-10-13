package c.e.mapper;

import c.e.entity.dto.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

//用户相关查询方法，使用MyBatisPlus自动生成代码
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
