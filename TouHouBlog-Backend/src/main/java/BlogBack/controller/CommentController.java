package BlogBack.controller;

import BlogBack.common.result.Result;
import BlogBack.pojo.dto.CommentAddDTO;
import BlogBack.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
@RestController
@Tag( name = "评论接口")
public class CommentController {
    private final CommentService commentService;

    /**
     * 发表评论
     * @param commentAddDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "发表评论")
    public Result addComment(@RequestBody CommentAddDTO commentAddDTO){
        log.info("发表评论：{}给{}",commentAddDTO.getUserId(),commentAddDTO.getArticleId());
        commentService.addComment(commentAddDTO);
        return Result.success();
    }
}
