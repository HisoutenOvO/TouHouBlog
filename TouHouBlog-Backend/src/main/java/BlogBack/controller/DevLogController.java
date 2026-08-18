package BlogBack.controller;

import BlogBack.common.result.Result;
import BlogBack.pojo.dto.DevLogAddDTO;
import BlogBack.pojo.dto.DevLogUpdateDTO;
import BlogBack.pojo.vo.DevLogVO;
import BlogBack.service.DevLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devlogs")
@RequiredArgsConstructor
@Tag(name = "开发者日志接口")
@Slf4j
public class DevLogController {

    private final DevLogService devLogService;

    @GetMapping("/list")
    @Operation(summary = "获取开发者日志列表")
    public Result<List<DevLogVO>> list() {
        return Result.success(devLogService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单条开发者日志")
    public Result<DevLogVO> getById(@PathVariable Long id) {
        DevLogVO vo = devLogService.getById(id);
        if (vo == null) return Result.error("日志不存在");
        return Result.success(vo);
    }

    @PostMapping
    @Operation(summary = "新增开发者日志")
    public Result add(@RequestBody DevLogAddDTO dto) {
        devLogService.add(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改开发者日志")
    public Result update(@PathVariable Long id, @RequestBody DevLogUpdateDTO dto) {
        devLogService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除开发者日志")
    public Result delete(@PathVariable Long id) {
        devLogService.delete(id);
        return Result.success();
    }
}