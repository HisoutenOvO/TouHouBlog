package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.ArticleAddDTO;
import BlogBack.pojo.dto.ArticlePageQueryDTO;
import BlogBack.pojo.dto.ArticleUpdateDTO;
import BlogBack.pojo.vo.ArticleVO;

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
    void addArticle(ArticleAddDTO articleAddDTO);

    /**
     * 修改文章
     * @param articleUpdateDTO
     * @param id
     */
    void update(ArticleUpdateDTO articleUpdateDTO, Long id);
}
