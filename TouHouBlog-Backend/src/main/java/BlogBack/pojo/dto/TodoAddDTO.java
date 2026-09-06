package BlogBack.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoAddDTO {
    private Integer type;
    private String title;
    private Integer priority;
}