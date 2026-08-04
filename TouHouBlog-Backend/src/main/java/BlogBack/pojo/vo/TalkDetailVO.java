package BlogBack.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TalkDetailVO {

    private Long id;

    private String content;

    private String picture;

    private LocalDateTime createTime;

}
