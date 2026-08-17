package BlogBack.mapper;

import BlogBack.pojo.dto.ArticlePageQueryDTO;
import BlogBack.pojo.entity.Article;
import BlogBack.pojo.vo.ArchiveArticleVO;
import BlogBack.pojo.vo.ArticleVO;
import BlogBack.pojo.vo.TagVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
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

    /**
     * 根据文章和用户id查询点赞id
     * @param id
     * @param userId
     * @return
     */
    @Select("select id from like_record where article_id = #{id} and user_id = #{userId}")
    Long getLikeIdByArticleIdAndUserId(Long id, Long userId);

    /**
     * 根据点赞id删除点赞
     * @param likeId
     */
    @Delete("delete from like_record where id = #{likeId}")
    void deleteLike(Long likeId);

    /**
     * 根据文章和用户id添加点赞记录
     * @param id
     * @param userId
     */
    @Insert("insert into like_record (article_id,user_id) values (#{id},#{userId})")
    void addLike(Long id, Long userId);

    /**
     * 查询用户是否为文章点赞
     * @param id
     * @param userId
     * @return
     */
    @Select("select count(*) from like_record where article_id = #{id} and user_id = #{userId}")
    Integer ifUserLiked(Long id, Long userId);

    /**
     * 获取文章的点赞总数
     * @param id
     * @return
     */
    @Select("select count(*) from like_record where article_id = #{id}")
    Integer getLikeTotal(Long id);


    List<ArchiveArticleVO> selectArchiveList();

    ArticleVO selectLatestDraft();
}
