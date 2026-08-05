package BlogBack.service.impl;

import BlogBack.common.exception.ArticleNotExistException;
import BlogBack.common.result.PageResult;
import BlogBack.mapper.ArticleMapper;
import BlogBack.mapper.CategoryMapper;
import BlogBack.mapper.TagMapper;
import BlogBack.pojo.dto.ArticleAddDTO;
import BlogBack.pojo.dto.ArticlePageQueryDTO;
import BlogBack.pojo.dto.ArticleUpdateDTO;
import BlogBack.pojo.entity.Article;
import BlogBack.pojo.vo.ArticleVO;
import BlogBack.pojo.vo.TagVO;
import BlogBack.service.ArticleService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static BlogBack.common.constant.MessageConstant.ARTICLE_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleMapper articleMapper;
    private final TagMapper tagMapper;

    /**
     * 文章的分页条件查询
     * @param articlePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(ArticlePageQueryDTO articlePageQueryDTO) {
        // 第一步：PageHelper 分页查询文章（不含标签）
        PageHelper.startPage(articlePageQueryDTO.getPage(), articlePageQueryDTO.getPageSize());
        List<ArticleVO> articles = articleMapper.pageArticles(articlePageQueryDTO);
        Page<ArticleVO> articlePage = (Page<ArticleVO>) articles;

        if (!articlePage.isEmpty()) {
            // 第二步：收集文章 ID，批量查询标签
            List<Long> articleIds = articlePage.stream().map(ArticleVO::getId).toList();
            List<TagVO> allTags = articleMapper.getTagsByArticleIds(articleIds);

            // 组装：将标签按 articleId 分组填入对应文章
            Map<Long, List<TagVO>> tagMap = allTags.stream()
                    .collect(Collectors.groupingBy(TagVO::getArticleId));

            articlePage.forEach(vo -> vo.setTags(tagMap.getOrDefault(vo.getId(), List.of())));
        }

        return new PageResult(articlePage.getTotal(), articlePage.getResult());
    }

    /**
     * 查询文章详情
     * @param id
     * @return
     */
    @Override
    public ArticleVO getDetail(Long id) {
        ArticlePageQueryDTO dto = new ArticlePageQueryDTO();
        dto.setId(id);
        List<ArticleVO> articles = articleMapper.pageArticles(dto);
        if (articles.isEmpty()) throw new ArticleNotExistException(ARTICLE_NOT_EXIST);
        ArticleVO article = articles.get(0);

        // 查标签
        List<TagVO> tags = articleMapper.getTagsByArticleIds(List.of(id));
        article.setTags(tags);
        return article;
    }

    /**
     * 新增文章
     * @param articleAddDTO
     */
    @Override
    @Transactional
    public void addArticle(ArticleAddDTO articleAddDTO) {
        Article article = new Article();
        BeanUtils.copyProperties(articleAddDTO,article);
        articleMapper.insert(article);
        List<Integer> tagIds = articleAddDTO.getTagIds();
        tagMapper.insertBatch(article.getId(),tagIds);
    }

    /**
     * 修改文章
     * @param articleUpdateDTO
     * @param id
     */
    @Override
    public void update(ArticleUpdateDTO articleUpdateDTO, Long id) {
        //先修改文章
        Article article = articleMapper.selectById(id);
        if(article == null){
            throw new ArticleNotExistException(ARTICLE_NOT_EXIST);
        }
        BeanUtils.copyProperties(articleUpdateDTO,article);
        articleMapper.updateById(id,article);
        if(articleUpdateDTO.getTagIds() != null && !articleUpdateDTO.getTagIds().isEmpty()) {
            //再修改文章相关的标签（先删再加）
            tagMapper.deleteBatch(id);
            tagMapper.insertBatch(id, articleUpdateDTO.getTagIds());
        }
    }

    /**
     * 删除文章
     * @param id
     */
    @Override
    public void delete(Long id) {
        //先删除文章
        articleMapper.deleteById(id);
        //再删除相关联的标签
        tagMapper.deleteBatch(id);
    }
}
