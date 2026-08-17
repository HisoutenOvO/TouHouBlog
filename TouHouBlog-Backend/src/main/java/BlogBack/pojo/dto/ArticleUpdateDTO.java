package BlogBack.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleUpdateDTO {

    private String title;

    private String content;

    private String coverUrl;

    private Long categoryId;

    private List<Integer> tagIds;

    private Integer status;   // 0=草稿，1=已发布
}
