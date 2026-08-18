package BlogBack.mapper;

import BlogBack.pojo.entity.DevLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DevLogMapper extends BaseMapper<DevLog> {
    List<DevLog> selectAllOrderByCreateTimeDesc();
}