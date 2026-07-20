package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.ArticleAddDTO;
import BlogBack.pojo.dto.ArticlePageQueryDTO;
import BlogBack.pojo.dto.ArticleUpdateDTO;
import BlogBack.pojo.vo.ArticleVO;
import BlogBack.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping
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
    public Result AddArticle(@RequestBody ArticleAddDTO articleAddDTO){
        log.info("新增文章:{}",articleAddDTO.getTitle());
        articleService.addArticle(articleAddDTO);
        return Result.success();
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
}
