package BlogBack.controller;

import BlogBack.common.result.Result;
import BlogBack.pojo.dto.TodoAddDTO;
import BlogBack.pojo.dto.TodoSubtaskAddDTO;
import BlogBack.pojo.dto.TodoSubtaskUpdateDTO;
import BlogBack.pojo.dto.TodoUpdateDTO;
import BlogBack.pojo.entity.User;
import BlogBack.pojo.vo.StageTaskVO;
import BlogBack.pojo.vo.TodoVO;
import BlogBack.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 每日待办与阶段性任务接口
 */
@RestController
@RequestMapping("/todo")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "待办接口")
public class TodoController {

    private final TodoService todoService;

    /**
     * 获取每日待办列表
     * @param authentication 当前登录用户认证信息
     * @return 每日待办列表
     */
    @GetMapping("/daily")
    @Operation(summary = "获取每日待办列表")
    public Result<List<TodoVO>> listDailyTodos(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("获取每日待办列表, userId:{}", user.getId());
        List<TodoVO> list = todoService.listDailyTodos(user.getId());
        return Result.success(list);
    }

    /**
     * 获取阶段性任务列表（含子任务）
     * @param authentication 当前登录用户认证信息
     * @return 阶段性任务列表
     */
    @GetMapping("/stage")
    @Operation(summary = "获取阶段性任务列表")
    public Result<List<StageTaskVO>> listStageTasks(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("获取阶段性任务列表, userId:{}", user.getId());
        List<StageTaskVO> list = todoService.listStageTasks(user.getId());
        return Result.success(list);
    }

    /**
     * 新增主任务（每日待办或阶段性任务）
     * @param dto 新增任务数据
     * @param authentication 当前登录用户认证信息
     * @return 新任务ID
     */
    @PostMapping
    @Operation(summary = "新增待办/阶段性任务")
    public Result<Long> addTodo(@RequestBody TodoAddDTO dto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("新增待办, type:{}, title:{}", dto.getType(), dto.getTitle());
        Long newId = todoService.addTodo(dto, user.getId());
        return Result.success(newId);
    }

    /**
     * 编辑主任务
     * @param id 任务ID
     * @param dto 修改数据
     * @param authentication 当前登录用户认证信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑待办/阶段性任务")
    public Result updateTodo(@PathVariable Long id, @RequestBody TodoUpdateDTO dto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("编辑待办:{}", id);
        todoService.updateTodo(id, dto, user.getId());
        return Result.success();
    }

    /**
     * 删除主任务
     * @param id 任务ID
     * @param authentication 当前登录用户认证信息
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除待办/阶段性任务")
    public Result deleteTodo(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("删除待办:{}", id);
        todoService.deleteTodo(id, user.getId());
        return Result.success();
    }

    /**
     * 新增子任务
     * @param dto 新增子任务数据
     * @param authentication 当前登录用户认证信息
     * @return 新子任务ID
     */
    @PostMapping("/subtask")
    @Operation(summary = "新增子任务")
    public Result<Long> addSubtask(@RequestBody TodoSubtaskAddDTO dto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("新增子任务, mainId:{}", dto.getMainId());
        Long newId = todoService.addSubtask(dto, user.getId());
        return Result.success(newId);
    }

    /**
     * 编辑子任务
     * @param subtaskId 子任务ID
     * @param dto 修改数据
     * @param authentication 当前登录用户认证信息
     * @return 操作结果
     */
    @PutMapping("/subtask/{subtaskId}")
    @Operation(summary = "编辑子任务")
    public Result updateSubtask(@PathVariable Long subtaskId, @RequestBody TodoSubtaskUpdateDTO dto, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("编辑子任务:{}", subtaskId);
        todoService.updateSubtask(subtaskId, dto, user.getId());
        return Result.success();
    }

    /**
     * 删除子任务
     * @param subtaskId 子任务ID
     * @param authentication 当前登录用户认证信息
     * @return 操作结果
     */
    @DeleteMapping("/subtask/{subtaskId}")
    @Operation(summary = "删除子任务")
    public Result deleteSubtask(@PathVariable Long subtaskId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        log.info("删除子任务:{}", subtaskId);
        todoService.deleteSubtask(subtaskId, user.getId());
        return Result.success();
    }
}