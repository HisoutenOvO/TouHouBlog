package BlogBack.service;

import BlogBack.pojo.vo.LikeVO;

public interface TalkLikeService {
    LikeVO toggleLike(Long talkId, Long userId);
    LikeVO getLikeStatus(Long talkId, Long userId);
}