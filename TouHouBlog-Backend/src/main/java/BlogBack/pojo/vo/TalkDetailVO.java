package BlogBack.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TalkDetailVO {

    private Long id;

    private String content;

    private List<String> pictures;

    private LocalDateTime createTime;

}
