package BlogBack.mapper;

import BlogBack.pojo.dto.TagPageQueryDTO;
import BlogBack.pojo.entity.Tag;
import BlogBack.pojo.vo.TagPageQueryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 标签分页查询
     * @param tagPageQueryDTO
     * @return
     */
    Page<TagPageQueryVO> pageQuery(TagPageQueryDTO tagPageQueryDTO);

    /**
     * 根据标签id查询是否有关联文章
     * @param id
     * @return
     */
    @Select("select count(*) from article_tag where tag_id = #{id}")
    Integer getRelativeArticleNumById(Long id);
}
