package BlogBack.service;

import BlogBack.pojo.dto.GalleryImageAddDTO;
import BlogBack.pojo.vo.GalleryImageVO;
import java.util.List;

public interface GalleryService {
    List<GalleryImageVO> listImages();
    void addImage(GalleryImageAddDTO dto);

    void deleteImage(Long id);
}