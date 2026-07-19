package BlogBack.mapper;

import BlogBack.pojo.entity.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
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

    /**
     * 根据文章id删除相应的文章——标签关联
     * @param articleId
     */
    @Delete("delete from article_tag where article_id = #{articleId}")
    void deleteBatch(Long articleId);
}
