package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.TalkAddDTO;
import BlogBack.pojo.dto.TalkPageQueryDTO;
import BlogBack.pojo.vo.TalkDetailVO;

public interface TalkService {
    /**
     * 杂谈的分页查询
     * @param talkPageQueryDTO
     * @return
     */
    PageResult pageQuery(TalkPageQueryDTO talkPageQueryDTO);

    /**
     * 新增杂谈
     * @param talkAddDTO
     */
    void add(TalkAddDTO talkAddDTO);

    /**
     * 根据id查询杂谈详情
     * @param id
     * @return
     */
    TalkDetailVO getById(Long id);

    /**
     * 删除杂谈
     * @param id
     */
    void delete(Long id);
}
