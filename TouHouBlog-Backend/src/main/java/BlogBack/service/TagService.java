package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.TagAddDTO;
import BlogBack.pojo.dto.TagPageQueryDTO;

public interface TagService {
    /**
     * 标签列表查询
     * @param tagPageQueryDTO
     * @return
     */
    PageResult pageQuery(TagPageQueryDTO tagPageQueryDTO);

    /**
     * 新增标签
     * @param tagAddDTO
     */
    void add(TagAddDTO tagAddDTO);
}
