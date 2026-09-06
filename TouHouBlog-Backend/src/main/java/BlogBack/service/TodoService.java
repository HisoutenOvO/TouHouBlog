package BlogBack.service;

import BlogBack.pojo.dto.TodoAddDTO;
import BlogBack.pojo.dto.TodoSubtaskAddDTO;
import BlogBack.pojo.dto.TodoSubtaskUpdateDTO;
import BlogBack.pojo.dto.TodoUpdateDTO;
import BlogBack.pojo.vo.StageTaskVO;
import BlogBack.pojo.vo.TodoVO;

import java.util.List;

/**
 * 待办与阶段性任务服务接口
 */
public interface TodoService {

    /**
     * 获取当前用户的每日待办列表
     * @param userId 站长用户ID
     * @return 每日待办列表
     */
    List<TodoVO> listDailyTodos(Long userId);

    /**
     * 获取当前用户的阶段性任务列表（含子任务）
     * @param userId 站长用户ID
     * @return 阶段性任务列表
     */
    List<StageTaskVO> listStageTasks(Long userId);

    /**
     * 新增主任务（每日待办或阶段性任务）
     * @param dto 新增任务数据
     * @param userId 站长用户ID
     * @return 新任务ID
     */
    Long addTodo(TodoAddDTO dto, Long userId);

    /**
     * 修改主任务内容/状态
     * @param id 任务ID
     * @param dto 修改数据
     * @param userId 站长用户ID
     */
    void updateTodo(Long id, TodoUpdateDTO dto, Long userId);

    /**
     * 删除主任务（同时删除其子任务）
     * @param id 任务ID
     * @param userId 站长用户ID
     */
    void deleteTodo(Long id, Long userId);

    /**
     * 新增子任务
     * @param dto 新增子任务数据
     * @param userId 站长用户ID
     * @return 新子任务ID
     */
    Long addSubtask(TodoSubtaskAddDTO dto, Long userId);

    /**
     * 修改子任务内容/状态，并自动更新父任务状态
     * @param subtaskId 子任务ID
     * @param dto 修改数据
     * @param userId 站长用户ID
     */
    void updateSubtask(Long subtaskId, TodoSubtaskUpdateDTO dto, Long userId);

    /**
     * 删除子任务，并自动更新父任务状态
     * @param subtaskId 子任务ID
     * @param userId 站长用户ID
     */
    void deleteSubtask(Long subtaskId, Long userId);
}