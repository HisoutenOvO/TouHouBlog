package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.TagPageQueryDTO;
import BlogBack.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
