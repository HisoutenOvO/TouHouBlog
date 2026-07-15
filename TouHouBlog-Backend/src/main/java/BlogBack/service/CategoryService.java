package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.CategoryDTO;
import BlogBack.pojo.dto.CategoryPageQueryDTO;
import BlogBack.pojo.vo.CategoryUpdateVO;

public interface CategoryService {
    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult list(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 新增分类
     * @param categoryDTO
     */
    void add(CategoryDTO categoryDTO);

    /**
     * 修改分类
     * @param categoryDTO
     * @param id
     */
    void update(CategoryDTO categoryDTO, Integer id);

    /**
     * 根据id查询分类信息
     * @param id
     * @return
     */
    CategoryUpdateVO getById(Long id);

    /**
     * 删除分类
     * @param id
     */
    void delete(Long id);
}
