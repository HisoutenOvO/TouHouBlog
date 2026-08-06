package BlogBack.service.impl;

import BlogBack.common.result.PageResult;
import BlogBack.mapper.CommentMapper;
import BlogBack.mapper.UserMapper;
import BlogBack.pojo.dto.CommentAddDTO;
import BlogBack.pojo.dto.CommentListDTO;
import BlogBack.pojo.entity.Comment;
import BlogBack.pojo.vo.CommentListVO;
import BlogBack.service.CommentService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    /**
     * 发表评论
     * @param commentAddDTO
     */
    @Override
    public void addComment(CommentAddDTO commentAddDTO) {
        //1.查询userId对应的nickname
        String nickname = userMapper.getNicknameById(commentAddDTO.getUserId());
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddDTO,comment);
        comment.setNickname(nickname);
        commentMapper.insert(comment);
    }

    /**
     * 获取文章评论列表
     * @param id
     * @param commentListDTO
     *
     * @return
     */
    @Override
    public PageResult getList(Long id, CommentListDTO commentListDTO) {
        PageHelper.startPage(commentListDTO.getPage(),commentListDTO.getPageSize());
        Page<CommentListVO> page = commentMapper.pageQuery(id);
        return new PageResult(page.getTotal(), page.getResult());
    }
}
