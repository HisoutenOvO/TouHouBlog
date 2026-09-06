package BlogBack.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodoSubtaskAddDTO {
    private Long mainId;
    private String title;
}