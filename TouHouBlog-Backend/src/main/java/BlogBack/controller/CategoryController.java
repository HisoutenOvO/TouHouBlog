package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.CategoryDTO;
import BlogBack.pojo.dto.CategoryPageQueryDTO;
import BlogBack.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增分类")
    public Result add(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类");
        categoryService.add(categoryDTO);
        return Result.success();
    }
}
