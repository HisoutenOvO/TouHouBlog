package BlogBack.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LyricLineVO {

    private double time;        // 时间秒

    private String text;        // 原文歌词

    private String translation; // 翻译歌词，可能为空

}