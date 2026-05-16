package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.CategoryPageQueryDTO;
import BlogBack.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分类接口
 */
@RestController
@RequestMapping("/categories")
@Slf4j
@Tag(name = "分类接口")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * 分类分页查询
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "分类分页查询")
    public Result<PageResult> list(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询");
        PageResult pageResult = categoryService.list(categoryPageQueryDTO);
        return Result.success(pageResult);
    }
}
