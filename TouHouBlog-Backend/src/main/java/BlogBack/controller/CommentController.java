package BlogBack.controller;

import BlogBack.common.result.PageResult;
import BlogBack.common.result.Result;
import BlogBack.pojo.dto.CommentAddDTO;
import BlogBack.pojo.dto.CommentListDTO;
import BlogBack.pojo.entity.User;
import BlogBack.pojo.vo.CommentListVO;
import BlogBack.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
@RestController
@Tag( name = "评论接口")
public class CommentController {
    private final CommentService commentService;

    /**
     * 发表评论
     * @param auth
     * @return
     */
    @PostMapping
    @Operation(summary = "发表评论")
    public Result add(@RequestBody CommentAddDTO dto, Authentication auth) {
        User user = (User) auth.getPrincipal();
        dto.setUserId(user.getId());  // 强制使用当前登录用户ID
        commentService.addComment(dto);
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

    /**
     * 获取杂谈评论列表
     * @param id
     * @param commentListDTO
     * @return
     */
    @GetMapping("/talk/{id}")
    @Operation(summary = "获取杂谈评论列表")
    public Result<PageResult> getTalkCommentList(@PathVariable Long id, CommentListDTO commentListDTO) {
        log.info("获取杂谈评论列表:{}", id);
        PageResult pageResult = commentService.getTalkList(id, commentListDTO);
        return Result.success(pageResult);
    }

}
