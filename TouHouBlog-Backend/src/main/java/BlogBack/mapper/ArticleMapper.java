package BlogBack.mapper;

import BlogBack.pojo.dto.ArticlePageQueryDTO;
import BlogBack.pojo.entity.Article;
import BlogBack.pojo.vo.ArticleVO;
import BlogBack.pojo.vo.TagVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 根据分类id查询文章
     * @param categoryId
     * @return
     */
    @Select("select * from article where category_id = #{categoryId}")
    List<Article> selectByCategoryId(Long categoryId);

    /**
     * 批量修改文章的分类
     * @param OldCategoryId
     * @param NewCategoryId
     */
    void updateArticleCategoryIdBatch(Long OldCategoryId,Long NewCategoryId);

    /**
     * 对文章的条件分页查询
     * @param dto
     * @return
     */
    List<ArticleVO> pageArticles(ArticlePageQueryDTO dto);

    List<TagVO> getTagsByArticleIds(List<Long> articleIds);

    /**
     * 根据id修改文章（重写mp方法）
     * @param id
     */
    void updateById(Long id,Article article);
}
