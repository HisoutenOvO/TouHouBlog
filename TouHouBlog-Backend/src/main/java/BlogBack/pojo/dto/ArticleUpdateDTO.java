package BlogBack.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleUpdateDTO {

    private Long id;

    private String title;

    private String content;

    private Long categoryId;

    private List<Integer> tagIds;

}
