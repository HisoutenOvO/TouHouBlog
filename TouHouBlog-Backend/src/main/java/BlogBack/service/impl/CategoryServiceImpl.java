package BlogBack.service.impl;

import BlogBack.common.exception.CategoryNotExistException;
import BlogBack.common.exception.NameHasBeenUsedException;
import BlogBack.common.result.PageResult;
import BlogBack.mapper.ArticleMapper;
import BlogBack.mapper.CategoryMapper;
import BlogBack.pojo.dto.CategoryDTO;
import BlogBack.pojo.dto.CategoryPageQueryDTO;
import BlogBack.pojo.entity.Article;
import BlogBack.pojo.entity.Category;
import BlogBack.pojo.vo.CategoryUpdateVO;
import BlogBack.service.CategoryService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static BlogBack.common.constant.MessageConstant.CATEGORY_NOT_EXIST;
import static BlogBack.common.constant.MessageConstant.NAME_HAS_BEEN_USED;
import static BlogBack.common.constant.NumberConstant.ElseCategoryId;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

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
            throw new NameHasBeenUsedException(NAME_HAS_BEEN_USED);
        }
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.insert(category);
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @param id
     */
    @Override
    public void update(CategoryDTO categoryDTO, Integer id) {
        Category category = categoryMapper.selectById(id);
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.updateById(category);
    }

    /**
     * 根据id查询分类信息
     * @param id
     * @return
     */
    @Override
    public CategoryUpdateVO getById(Long id) {
        Category category = categoryMapper.selectById(id);
        CategoryUpdateVO categoryUpdateVO = new CategoryUpdateVO();
        BeanUtils.copyProperties(category,categoryUpdateVO);
        return categoryUpdateVO;
    }

    /**
     * 删除分类
     * @param id
     */
    @Override
    public void delete(Long id) {
        if(categoryMapper.selectById(id) == null){
            //先判断该分类下是否有文章
            List<Article> articles = articleMapper.selectByCategoryId(id);
            if(articles == null){
                //先把分类下的各个文章全部转移给“其他”分类
                articleMapper.updateArticleCategoryIdBatch(id,ElseCategoryId);
            }
            categoryMapper.deleteById(id);
            //再删除该文章
        }
        else {
            throw new CategoryNotExistException(CATEGORY_NOT_EXIST);
        }
    }
}
