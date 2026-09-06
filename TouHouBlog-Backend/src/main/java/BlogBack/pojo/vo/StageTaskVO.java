package BlogBack.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StageTaskVO {
    private Long id;
    private String title;
    private Integer priority;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<TodoSubtaskVO> subtasks;
}