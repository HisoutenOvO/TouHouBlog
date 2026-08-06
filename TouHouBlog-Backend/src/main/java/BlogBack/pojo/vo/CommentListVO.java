package BlogBack.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentListVO {

    private Long id;

    private Long userId;

    private String nickname;

    private String content;

    private LocalDateTime createTime;
}
