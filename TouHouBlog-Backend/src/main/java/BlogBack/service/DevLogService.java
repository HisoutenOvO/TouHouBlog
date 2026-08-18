package BlogBack.service;

import BlogBack.pojo.dto.DevLogAddDTO;
import BlogBack.pojo.dto.DevLogUpdateDTO;
import BlogBack.pojo.vo.DevLogVO;
import java.util.List;

public interface DevLogService {
    List<DevLogVO> list();
    DevLogVO getById(Long id);
    void add(DevLogAddDTO dto);
    void update(Long id, DevLogUpdateDTO dto);
    void delete(Long id);
}