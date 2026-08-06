package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.CommentAddDTO;
import BlogBack.pojo.dto.CommentListDTO;
import BlogBack.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取文章评论列表
     * @return
     */
    @GetMapping("/article/{id}")
    @Operation(summary = "获取文章评论列表")
    public Result<PageResult> getCommentList(@PathVariable Long id, CommentListDTO commentListDTO){
        log.info("获取文章评论列表:{}",id);
        PageResult pageResult = commentService.getList(id,commentListDTO);
        return Result.success(pageResult);
    }
}
