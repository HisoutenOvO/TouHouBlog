package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.TagDTO;
import BlogBack.pojo.dto.TagPageQueryDTO;
import BlogBack.pojo.vo.TagVO;
import BlogBack.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 标签接口
 */
@RestController
@RequestMapping("/tags")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "标签接口")
public class TagController {
    private final TagService tagService;

    /**
     * 标签列表查询
     * @param tagPageQueryDTO
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "标签列表查询")
    public Result<PageResult> pageQuery(TagPageQueryDTO tagPageQueryDTO){
        log.info("标签列表查询");
        PageResult pageResult = tagService.pageQuery(tagPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 新增标签
     * @return
     */
    @PostMapping
    @Operation(summary = "新增标签")
    public Result add(@RequestBody TagDTO tagDTO){
        log.info("新增标签");
        tagService.add(tagDTO);
        return Result.success();
    }

    /**
     * 根据id查询标签
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查询标签")
    public Result<TagVO> getById(@PathVariable Long id){
        log.info("根据id查询标签:{}",id);
        TagVO tagVO = tagService.getById(id);
        return Result.success(tagVO);
    }

    /**
     * 修改标签
     * @return
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改标签")
    public Result update(@RequestBody TagDTO tagDTO,@PathVariable Long id){
        log.info("修改标签:{}",id);
        tagService.update(tagDTO,id);
        return Result.success();
    }
}
