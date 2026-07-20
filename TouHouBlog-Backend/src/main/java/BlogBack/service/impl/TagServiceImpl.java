package BlogBack.service.impl;

import BlogBack.common.result.PageResult;
import BlogBack.mapper.TagMapper;
import BlogBack.pojo.dto.TagPageQueryDTO;
import BlogBack.pojo.vo.TagPageQueryVO;
import BlogBack.service.TagService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagMapper tagMapper;

    /**
     * 标签列表查询
     * @param tagPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(TagPageQueryDTO tagPageQueryDTO) {
        PageHelper.startPage(tagPageQueryDTO.getPage(), tagPageQueryDTO.getPageSize());
        Page<TagPageQueryVO> page = tagMapper.pageQuery(tagPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
}
