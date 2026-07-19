package BlogBack.mapper;

import BlogBack.pojo.entity.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 插入和文章关联的标签
     * @param articleId
     * @param tagIds
     */
    void insertBatch(Long articleId, List<Integer> tagIds);
}
