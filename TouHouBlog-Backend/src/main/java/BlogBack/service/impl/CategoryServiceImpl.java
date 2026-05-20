package BlogBack.service.impl;

import BlogBack.common.exception.NameHasBeenUsedException;
import BlogBack.common.result.PageResult;
import BlogBack.mapper.CategoryMapper;
import BlogBack.pojo.dto.CategoryDTO;
import BlogBack.pojo.dto.CategoryPageQueryDTO;
import BlogBack.pojo.entity.Category;
import BlogBack.service.CategoryService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult list(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery();

        Long total = page.getTotal();
        List<Category> records = page.getResult();

        return new PageResult(total,records);
    }

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void add(CategoryDTO categoryDTO) {
        Category category = new Category();
        Category c = categoryMapper.selectByName(categoryDTO.getName());
        if(c != null){
            throw new NameHasBeenUsedException("已有该分类啦，换个名字吧~");
        }
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.insert(category);
    }
}
