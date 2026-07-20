package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.TalkPageQueryDTO;
import BlogBack.service.TalkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 杂谈接口
 */
@RestController
@RequestMapping("/talks")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "杂谈接口")
public class TalkController {
    private final TalkService talkService;

    /**
     * 杂谈分页查询
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "杂谈的分页查询")
    public Result<PageResult> pageQuery(TalkPageQueryDTO talkPageQueryDTO){
        log.info("杂谈分页查询");
        PageResult pageResult = talkService.pageQuery(talkPageQueryDTO);
        return Result.success(pageResult);
    }
}
