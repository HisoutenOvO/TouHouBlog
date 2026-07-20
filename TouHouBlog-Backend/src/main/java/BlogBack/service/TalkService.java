package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.TalkAddDTO;
import BlogBack.pojo.dto.TalkPageQueryDTO;

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
}
