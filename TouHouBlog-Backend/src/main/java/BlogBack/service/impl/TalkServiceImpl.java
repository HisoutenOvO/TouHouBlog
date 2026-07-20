package BlogBack.service.impl;

import BlogBack.common.result.PageResult;
import BlogBack.mapper.TalkMapper;
import BlogBack.pojo.dto.TalkPageQueryDTO;
import BlogBack.pojo.entity.Talk;
import BlogBack.service.TalkService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TalkServiceImpl implements TalkService {
    private final TalkMapper talkMapper;

    /**
     * 杂谈分页查询
     * @param talkPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(TalkPageQueryDTO talkPageQueryDTO) {
        PageHelper.startPage(talkPageQueryDTO.getPage(),talkPageQueryDTO.getPageSize());
        Page<Talk> page = talkMapper.pageQuery(talkPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
}
