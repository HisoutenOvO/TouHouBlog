package BlogBack.mapper;

import BlogBack.pojo.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
}
