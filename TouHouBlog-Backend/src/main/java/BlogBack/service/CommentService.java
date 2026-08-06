package BlogBack.service;

import BlogBack.common.result.PageResult;
import BlogBack.pojo.dto.CommentAddDTO;
import BlogBack.pojo.dto.CommentListDTO;


public interface CommentService {
    /**
     * 发表评论
     * @param commentAddDTO
     */
     void addComment(CommentAddDTO commentAddDTO);

    /**
     * 获取文章评论列表
     * @param id
     * @param commentListDTO
     * @return
     */
    PageResult getList(Long id, CommentListDTO commentListDTO);
}
