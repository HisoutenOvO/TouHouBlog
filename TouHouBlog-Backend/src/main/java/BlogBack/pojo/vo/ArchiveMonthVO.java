package BlogBack.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ArchiveMonthVO {
    private String month;                       // 如 2026-08
    private List<ArchiveArticleVO> articles;    // 该月文章列表
}