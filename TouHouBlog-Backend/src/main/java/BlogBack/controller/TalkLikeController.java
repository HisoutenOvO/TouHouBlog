package BlogBack.controller;

import BlogBack.common.result.Result;
import BlogBack.pojo.entity.User;
import BlogBack.pojo.vo.LikeVO;
import BlogBack.service.TalkLikeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/talks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "杂谈点赞接口")
public class TalkLikeController {
    private final TalkLikeService talkLikeService;

    @PostMapping("/{id}/like")
    public Result<LikeVO> toggleLike(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        LikeVO likeVO = talkLikeService.toggleLike(id, user.getId());
        return Result.success(likeVO);
    }

    @GetMapping("/{id}/like")
    public Result<LikeVO> getLikeStatus(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        LikeVO likeVO = talkLikeService.getLikeStatus(id, user.getId());
        return Result.success(likeVO);
    }
}