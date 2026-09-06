package BlogBack.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodoSubtaskVO {
    private Long id;
    private String title;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}