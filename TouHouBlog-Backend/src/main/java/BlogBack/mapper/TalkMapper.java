package BlogBack.mapper;

import BlogBack.pojo.dto.TalkPageQueryDTO;
import BlogBack.pojo.entity.Talk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TalkMapper extends BaseMapper<Talk> {
    /**
     * 杂谈的分页查询
     * @param talkPageQueryDTO
     * @return
     */
    @Select("select * from talk order by create_time desc")
    Page<Talk> pageQuery(TalkPageQueryDTO talkPageQueryDTO);
}
