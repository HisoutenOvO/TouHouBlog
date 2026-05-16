package BlogBack.mapper;

import BlogBack.pojo.entity.Talk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TalkMapper extends BaseMapper<Talk> {
}
