package BlogBack.mapper;

import BlogBack.pojo.entity.GalleryImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GalleryImageMapper extends BaseMapper<GalleryImage> {
    List<GalleryImage> selectAllOrderByCreateTimeDesc();
}