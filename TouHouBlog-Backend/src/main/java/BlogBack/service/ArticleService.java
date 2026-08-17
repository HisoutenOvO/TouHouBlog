package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.ArticleAddDTO;
import BlogBack.pojo.dto.ArticlePageQueryDTO;
import BlogBack.pojo.dto.ArticleUpdateDTO;
import BlogBack.pojo.vo.ArchiveMonthVO;
import BlogBack.pojo.vo.ArticleVO;
import BlogBack.pojo.vo.LikeVO;

import java.util.List;

public interface ArticleService {
    /**
     * 文章的分页条件查询
     * @param articlePageQueryDTO
     * @return
     */
    PageResult pageQuery(ArticlePageQueryDTO articlePageQueryDTO);

    /**
     * 查询文章详情
     * @param id
     * @return
     */
    ArticleVO getDetail(Long id);

    /**
     * 新增文章
     * @param articleAddDTO
     */
    Long addArticle(ArticleAddDTO articleAddDTO);

    /**
     * 修改文章
     * @param articleUpdateDTO
     * @param id
     */
    void update(ArticleUpdateDTO articleUpdateDTO, Long id);

    /**
     * 删除文章
     * @param id
     */
    void delete(Long id);

    /**
     * 点赞/取消点赞
     * @param articleId
     * @param userId
     * @return
     */
    LikeVO toggleLike(Long articleId, Long userId);

    /**
     * 查询点赞状态
     * @param articleId
     * @param userId
     * @return
     */
    LikeVO getLikeStatus(Long articleId, Long userId);

    List<ArchiveMonthVO> getArchiveList();

    ArticleVO getDraft();
}
