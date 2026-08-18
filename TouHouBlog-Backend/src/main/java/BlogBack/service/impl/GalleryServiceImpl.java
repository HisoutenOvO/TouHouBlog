package BlogBack.service.impl;

import BlogBack.mapper.GalleryImageMapper;
import BlogBack.pojo.dto.GalleryImageAddDTO;
import BlogBack.pojo.entity.GalleryImage;
import BlogBack.pojo.vo.GalleryImageVO;
import BlogBack.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {

    private final GalleryImageMapper galleryImageMapper;

    @Override
    public List<GalleryImageVO> listImages() {
        return galleryImageMapper.selectAllOrderByCreateTimeDesc().stream().map(entity -> {
            GalleryImageVO vo = new GalleryImageVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void addImage(GalleryImageAddDTO dto) {
        GalleryImage image = new GalleryImage();
        image.setUrl(dto.getUrl());
        galleryImageMapper.insert(image);
    }

    @Override
    public void deleteImage(Long id) {
        galleryImageMapper.deleteById(id);
    }
}