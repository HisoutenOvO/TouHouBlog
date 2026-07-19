package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.CategoryDTO;
import BlogBack.pojo.dto.CategoryPageQueryDTO;
import BlogBack.pojo.vo.CategoryUpdateVO;
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

    /**
     * 修改分类
     * @param categoryDTO
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO, @PathVariable Long id){
        log.info("修改分类:{}",id);
        categoryService.update(categoryDTO,id);
        return Result.success();
    }

    /**
     * 根据id查询分类信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查询分类信息")
    public Result<CategoryUpdateVO> getByCategoryId(@PathVariable Long id){
        log.info("根据id查询分类信息:{}",id);
        CategoryUpdateVO categoryUpdateVO = categoryService.getById(id);
        return Result.success(categoryUpdateVO);
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result delete(@PathVariable Long id){
        log.info("删除分类:{}",id);
        categoryService.delete(id);
        return Result.success();
    }
}
