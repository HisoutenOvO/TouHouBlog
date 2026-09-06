package BlogBack.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodoVO {
    private Long id;
    private Integer type;
    private String title;
    private Integer priority;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}