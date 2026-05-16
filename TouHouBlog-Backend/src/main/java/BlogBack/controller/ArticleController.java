package BlogBack.controller;

import BlogBack.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章接口
 */
@RestController
@RequestMapping("/articles")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "文章接口")
public class ArticleController {
    private final ArticleService articleService;
}
