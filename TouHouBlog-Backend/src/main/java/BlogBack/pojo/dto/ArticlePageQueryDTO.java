package BlogBack.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticlePageQueryDTO {

     private Long id;

     private int page;

     private int pageSize;

     private String title;

     private Long categoryId;

     private Long tagId;
}
