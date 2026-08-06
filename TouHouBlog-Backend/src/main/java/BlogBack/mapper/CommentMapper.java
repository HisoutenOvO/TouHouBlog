package BlogBack.mapper;

import BlogBack.pojo.entity.Comment;
import BlogBack.pojo.vo.CommentListVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 文章的评论列表查询
     * @param articleId
     * @return
     */
    Page<CommentListVO> pageQuery(Long articleId);
}
