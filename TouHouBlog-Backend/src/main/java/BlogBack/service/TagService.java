package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.TagDTO;
import BlogBack.pojo.dto.TagPageQueryDTO;
import BlogBack.pojo.vo.TagVO;

public interface TagService {
    /**
     * 标签列表查询
     * @param tagPageQueryDTO
     * @return
     */
    PageResult pageQuery(TagPageQueryDTO tagPageQueryDTO);

    /**
     * 新增标签
     * @param tagDTO
     */
    void add(TagDTO tagDTO);

    /**
     * 根据id查询标签
     * @param id
     * @return
     */
    TagVO getById(Long id);

    /**
     * 修改标签
     * @param tagDTO
     * @param id
     */
    void update(TagDTO tagDTO, Long id);
}
