package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.CategoryPageQueryDTO;

public interface CategoryService {
    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult list(CategoryPageQueryDTO categoryPageQueryDTO);
}
