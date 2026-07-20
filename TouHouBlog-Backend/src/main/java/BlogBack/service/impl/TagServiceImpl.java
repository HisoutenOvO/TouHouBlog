package BlogBack.service.impl;

import BlogBack.common.result.PageResult;
import BlogBack.mapper.TagMapper;
import BlogBack.pojo.dto.TagDTO;
import BlogBack.pojo.dto.TagPageQueryDTO;
import BlogBack.pojo.entity.Tag;
import BlogBack.pojo.vo.TagPageQueryVO;
import BlogBack.pojo.vo.TagVO;
import BlogBack.service.TagService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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

    /**
     * 新增标签
     * @param tagDTO
     */
    @Override
    public void add(TagDTO tagDTO) {
        Tag tag = new Tag();
        tag.setName(tagDTO.getName());
        tagMapper.insert(tag);
    }

    /**
     * 根据id查询标签
     * @param id
     * @return
     */
    @Override
    public TagVO getById(Long id) {
        Tag tag = tagMapper.selectById(id);
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag,tagVO);
        return tagVO;
    }

    /**
     * 修改标签
     * @param tagDTO
     * @param id
     */
    @Override
    public void update(TagDTO tagDTO, Long id) {
        Tag tag = tagMapper.selectById(id);
        tag.setName(tagDTO.getName());
        tagMapper.updateById(tag);
    }

}
