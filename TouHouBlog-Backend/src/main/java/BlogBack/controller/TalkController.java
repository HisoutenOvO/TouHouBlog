package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.TalkAddDTO;
import BlogBack.pojo.dto.TalkPageQueryDTO;
import BlogBack.pojo.vo.TalkDetailVO;
import BlogBack.service.TalkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 新增杂谈
     * @return
     */
    @PostMapping
    @Operation(summary = "新增杂谈")
    public Result add(@RequestBody TalkAddDTO talkAddDTO){
        log.info("新增杂谈");
        talkService.add(talkAddDTO);
        return Result.success();
    }

    /**
     * 根据id查看杂谈详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查看杂谈详情")
    public Result<TalkDetailVO> getById(@PathVariable Long id){
        log.info("根据id查看杂谈详情:{}",id);
        TalkDetailVO talkDetailVO = talkService.getById(id);
        return Result.success(talkDetailVO);
    }

    /**
     * 删除杂谈
     * @return
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除杂谈")
    public Result delete(@PathVariable Long id){
        log.info("删除杂谈:{}",id);
        talkService.delete(id);
        return Result.success();
    }
}
