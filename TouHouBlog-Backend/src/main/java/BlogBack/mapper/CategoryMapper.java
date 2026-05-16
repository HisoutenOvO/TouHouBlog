package BlogBack.mapper;

import BlogBack.pojo.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    /**
     * 分页查询分类列表
     * @return
     */
    Page<Category> pageQuery();
}
