package BlogBack.controller;

import BlogBack.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分类接口
 */
@RestController
@RequestMapping("/categories")
@Slf4j
@Tag(name = "分类接口")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
}
