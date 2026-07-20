package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.TagPageQueryDTO;

public interface TagService {
    /**
     * 标签列表查询
     * @param tagPageQueryDTO
     * @return
     */
    PageResult pageQuery(TagPageQueryDTO tagPageQueryDTO);
}
