package BlogBack.service.impl;

import BlogBack.mapper.TodoMainMapper;
import BlogBack.mapper.TodoSubtaskMapper;
import BlogBack.pojo.dto.TodoAddDTO;
import BlogBack.pojo.dto.TodoSubtaskAddDTO;
import BlogBack.pojo.dto.TodoSubtaskUpdateDTO;
import BlogBack.pojo.dto.TodoUpdateDTO;
import BlogBack.pojo.entity.TodoMain;
import BlogBack.pojo.entity.TodoSubtask;
import BlogBack.pojo.vo.StageTaskVO;
import BlogBack.pojo.vo.TodoSubtaskVO;
import BlogBack.pojo.vo.TodoVO;
import BlogBack.service.TodoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 待办与阶段性任务服务实现
 */
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoMainMapper todoMainMapper;
    private final TodoSubtaskMapper todoSubtaskMapper;

    /**
     * 获取每日待办列表（按重要程度降序，创建时间升序）
     */
    @Override
    public List<TodoVO> listDailyTodos(Long userId) {
        LambdaQueryWrapper<TodoMain> wrapper = new LambdaQueryWrapper<TodoMain>()
                .eq(TodoMain::getUserId, userId)
                .eq(TodoMain::getType, 1)
                .orderByDesc(TodoMain::getPriority)
                .orderByAsc(TodoMain::getCreateTime);

        return todoMainMapper.selectList(wrapper).stream().map(main -> {
            TodoVO vo = new TodoVO();
            BeanUtils.copyProperties(main, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取阶段性任务列表（含子任务，按重要程度降序）
     */
    @Override
    public List<StageTaskVO> listStageTasks(Long userId) {
        // 查询主任务
        LambdaQueryWrapper<TodoMain> mainWrapper = new LambdaQueryWrapper<TodoMain>()
                .eq(TodoMain::getUserId, userId)
                .eq(TodoMain::getType, 2)
                .orderByDesc(TodoMain::getPriority)
                .orderByAsc(TodoMain::getCreateTime);
        List<TodoMain> mains = todoMainMapper.selectList(mainWrapper);
        if (mains.isEmpty()) return new ArrayList<>();

        // 批量查询子任务
        List<Long> mainIds = mains.stream().map(TodoMain::getId).collect(Collectors.toList());
        LambdaQueryWrapper<TodoSubtask> subtaskWrapper = new LambdaQueryWrapper<TodoSubtask>()
                .in(TodoSubtask::getMainId, mainIds)
                .orderByAsc(TodoSubtask::getCreateTime);
        List<TodoSubtask> subtasks = todoSubtaskMapper.selectList(subtaskWrapper);

        // 按父任务ID分组
        Map<Long, List<TodoSubtask>> subtaskMap = subtasks.stream()
                .collect(Collectors.groupingBy(TodoSubtask::getMainId));

        return mains.stream().map(main -> {
            StageTaskVO vo = new StageTaskVO();
            BeanUtils.copyProperties(main, vo);
            List<TodoSubtask> subs = subtaskMap.getOrDefault(main.getId(), new ArrayList<>());
            List<TodoSubtaskVO> subVos = subs.stream().map(sub -> {
                TodoSubtaskVO subVo = new TodoSubtaskVO();
                BeanUtils.copyProperties(sub, subVo);
                return subVo;
            }).collect(Collectors.toList());
            vo.setSubtasks(subVos);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 新增主任务
     */
    @Override
    public Long addTodo(TodoAddDTO dto, Long userId) {
        TodoMain main = new TodoMain();
        main.setUserId(userId);
        main.setType(dto.getType());
        main.setTitle(dto.getTitle());
        main.setPriority(dto.getPriority() != null ? dto.getPriority() : 3);
        main.setStatus(0);
        main.setSortOrder(0);
        todoMainMapper.insert(main);
        return main.getId();
    }

    /**
     * 修改主任务，若阶段性任务被手动标记完成，则所有子任务一并完成
     */
    @Override
    @Transactional
    public void updateTodo(Long id, TodoUpdateDTO dto, Long userId) {
        TodoMain main = todoMainMapper.selectById(id);
        if (main == null || !main.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权操作");
        }

        if (dto.getTitle() != null) main.setTitle(dto.getTitle());
        if (dto.getPriority() != null) main.setPriority(dto.getPriority());
        if (dto.getStatus() != null) {
            main.setStatus(dto.getStatus());
            if (main.getType() == 2 && dto.getStatus() == 1) {
                // 阶段性任务标记完成，所有子任务都完成
                LambdaQueryWrapper<TodoSubtask> wrapper = new LambdaQueryWrapper<TodoSubtask>()
                        .eq(TodoSubtask::getMainId, id);
                List<TodoSubtask> subs = todoSubtaskMapper.selectList(wrapper);
                for (TodoSubtask sub : subs) {
                    sub.setStatus(1);
                    todoSubtaskMapper.updateById(sub);
                }
            }
        }
        todoMainMapper.updateById(main);
    }

    /**
     * 删除主任务及其所有子任务
     */
    @Override
    @Transactional
    public void deleteTodo(Long id, Long userId) {
        TodoMain main = todoMainMapper.selectById(id);
        if (main == null || !main.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权操作");
        }
        LambdaQueryWrapper<TodoSubtask> wrapper = new LambdaQueryWrapper<TodoSubtask>()
                .eq(TodoSubtask::getMainId, id);
        todoSubtaskMapper.delete(wrapper);
        todoMainMapper.deleteById(id);
    }

    /**
     * 新增子任务
     */
    @Override
    public Long addSubtask(TodoSubtaskAddDTO dto, Long userId) {
        TodoMain main = todoMainMapper.selectById(dto.getMainId());
        if (main == null || !main.getUserId().equals(userId) || main.getType() != 2) {
            throw new RuntimeException("阶段性任务不存在或无权操作");
        }

        TodoSubtask sub = new TodoSubtask();
        sub.setMainId(dto.getMainId());
        sub.setTitle(dto.getTitle());
        sub.setStatus(0);
        sub.setSortOrder(0);
        todoSubtaskMapper.insert(sub);
        return sub.getId();
    }

    /**
     * 修改子任务，并根据子任务完成情况自动更新父任务状态
     */
    @Override
    @Transactional
    public void updateSubtask(Long subtaskId, TodoSubtaskUpdateDTO dto, Long userId) {
        TodoSubtask sub = todoSubtaskMapper.selectById(subtaskId);
        if (sub == null) throw new RuntimeException("子任务不存在");

        TodoMain main = todoMainMapper.selectById(sub.getMainId());
        if (main == null || !main.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        if (dto.getTitle() != null) {
            sub.setTitle(dto.getTitle());
        }
        if (dto.getStatus() != null) {
            sub.setStatus(dto.getStatus());
        }

        // 先保存子任务状态
        todoSubtaskMapper.updateById(sub);

        // 然后根据所有子任务状态更新父任务状态
        if (dto.getStatus() != null) {
            updateMainStatusBySubtasks(main.getId());
        }
    }

    /**
     * 删除子任务，并重新计算父任务状态
     */
    @Override
    @Transactional
    public void deleteSubtask(Long subtaskId, Long userId) {
        TodoSubtask sub = todoSubtaskMapper.selectById(subtaskId);
        if (sub == null) return;

        TodoMain main = todoMainMapper.selectById(sub.getMainId());
        if (main == null || !main.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        todoSubtaskMapper.deleteById(subtaskId);
        updateMainStatusBySubtasks(main.getId());
    }

    /**
     * 根据所有子任务状态更新父任务状态
     * 所有子任务完成 → 父任务完成；否则父任务未完成
     */
    private void updateMainStatusBySubtasks(Long mainId) {
        LambdaQueryWrapper<TodoSubtask> wrapper = new LambdaQueryWrapper<TodoSubtask>()
                .eq(TodoSubtask::getMainId, mainId);
        List<TodoSubtask> subs = todoSubtaskMapper.selectList(wrapper);
        TodoMain main = todoMainMapper.selectById(mainId);
        if (main == null) return;

        if (subs.isEmpty()) {
            main.setStatus(0);
            todoMainMapper.updateById(main);
            return;
        }

        boolean allDone = subs.stream().allMatch(sub -> sub.getStatus() == 1);
        main.setStatus(allDone ? 1 : 0);
        todoMainMapper.updateById(main);
    }
}