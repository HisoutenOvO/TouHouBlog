package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.ArticleAddDTO;
import BlogBack.pojo.dto.ArticlePageQueryDTO;
import BlogBack.pojo.dto.ArticleUpdateDTO;
import BlogBack.pojo.entity.User;
import BlogBack.pojo.vo.ArchiveMonthVO;
import BlogBack.pojo.vo.ArticleVO;
import BlogBack.pojo.vo.LikeVO;
import BlogBack.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章接口
 */
@RestController
@RequestMapping("/articles")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "文章接口")
public class ArticleController {
    private final ArticleService articleService;

    /**
     * 文章列表条件查询
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "文章列表条件查询")
    public Result<PageResult> listQuery(ArticlePageQueryDTO articlePageQueryDTO) {
        log.info("分页查询文章列表");
        PageResult pageResult = articleService.pageQuery(articlePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询文章详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询文章详情")
    public Result<ArticleVO> getDetail(@PathVariable Long id) {
        log.info("查询文章详情:{}", id);
        ArticleVO articleVO = articleService.getDetail(id);
        return Result.success(articleVO);
    }

    /**
     * 新增文章
     * @return
     */
    @PostMapping
    @Operation(summary = "新增文章")
    public Result AddArticle(@RequestBody ArticleAddDTO articleAddDTO) {
        log.info("新增文章:{}", articleAddDTO.getTitle());
        Long newId = articleService.addArticle(articleAddDTO);
        return Result.success(newId);
    }

    /**
     * 编辑文章
     * @return
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑文章")
    public Result update(@RequestBody ArticleUpdateDTO articleUpdateDTO,@PathVariable Long id){
        log.info("修改文章:{}",id);
        articleService.update(articleUpdateDTO,id);
        return Result.success();
    }

    /**
     * 删除文章
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章")
    public Result delete(@PathVariable Long id){
        log.info("删除文章:{}",id);
        articleService.delete(id);
        return Result.success();
    }

    /**
     * 点赞/取消点赞
     * @return
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞/取消点赞")
    public Result<LikeVO> like(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        LikeVO likeVO = articleService.toggleLike(id, user.getId());
        return Result.success(likeVO);
    }
    /**
     * 查询点赞状态
     * @return
     */
    @GetMapping("/{id}/like")
    @Operation(summary = "查询点赞状态")
    public Result<LikeVO> getLikeStatus(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        LikeVO likeVO = articleService.getLikeStatus(id, user.getId());
        return Result.success(likeVO);
    }

    /**
     * 文章归档时间线
     * @return
     */
    @GetMapping("/archive")
    @Operation(summary = "文章归档时间线")
    public Result<List<ArchiveMonthVO>> getArchiveList() {
        log.info("获取文章归档时间线");
        return Result.success(articleService.getArchiveList());
    }

    /**
     * 获取最新草稿
     * @return
     */
    @GetMapping("/draft")
    @Operation(summary = "获取最新草稿")
    public Result<ArticleVO> getDraft() {
        log.info("获取最新草稿");
        ArticleVO draft = articleService.getDraft();
        return Result.success(draft);
    }
}
