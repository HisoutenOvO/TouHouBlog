package BlogBack.service.impl;

import BlogBack.mapper.DevLogMapper;
import BlogBack.pojo.dto.DevLogAddDTO;
import BlogBack.pojo.dto.DevLogUpdateDTO;
import BlogBack.pojo.entity.DevLog;
import BlogBack.pojo.vo.DevLogVO;
import BlogBack.service.DevLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DevLogServiceImpl implements DevLogService {

    private final DevLogMapper devLogMapper;

    @Override
    public List<DevLogVO> list() {
        return devLogMapper.selectAllOrderByCreateTimeDesc().stream().map(entity -> {
            DevLogVO vo = new DevLogVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public DevLogVO getById(Long id) {
        DevLog entity = devLogMapper.selectById(id);
        if (entity == null) return null;
        DevLogVO vo = new DevLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void add(DevLogAddDTO dto) {
        DevLog entity = new DevLog();
        BeanUtils.copyProperties(dto, entity);
        devLogMapper.insert(entity);
    }

    @Override
    public void update(Long id, DevLogUpdateDTO dto) {
        DevLog entity = devLogMapper.selectById(id);
        if (entity == null) return;
        BeanUtils.copyProperties(dto, entity);
        devLogMapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        devLogMapper.deleteById(id);
    }
}