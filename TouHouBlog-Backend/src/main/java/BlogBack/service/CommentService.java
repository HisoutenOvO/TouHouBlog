package BlogBack.service;

import BlogBack.pojo.dto.CommentAddDTO;

public interface CommentService {
    /**
     * 发表评论
     * @param commentAddDTO
     */
     void addComment(CommentAddDTO commentAddDTO);
}
