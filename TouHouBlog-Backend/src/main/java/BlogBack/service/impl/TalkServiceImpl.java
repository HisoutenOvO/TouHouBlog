package BlogBack.service.impl;

import BlogBack.common.result.PageResult;
import BlogBack.mapper.CommentMapper;
import BlogBack.mapper.LikeRecordMapper;
import BlogBack.mapper.TalkMapper;
import BlogBack.pojo.dto.TalkAddDTO;
import BlogBack.pojo.dto.TalkPageQueryDTO;
import BlogBack.pojo.entity.Comment;
import BlogBack.pojo.entity.LikeRecord;
import BlogBack.pojo.entity.Talk;
import BlogBack.pojo.vo.TalkDetailVO;
import BlogBack.service.TalkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalkServiceImpl implements TalkService {

    private final TalkMapper talkMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult pageQuery(TalkPageQueryDTO talkPageQueryDTO) {
        PageHelper.startPage(talkPageQueryDTO.getPage(), talkPageQueryDTO.getPageSize());
        Page<Talk> page = talkMapper.pageQuery(talkPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(TalkAddDTO talkAddDTO) {
        Talk talk = new Talk();
        talk.setContent(talkAddDTO.getContent());
        if (talkAddDTO.getPictures() != null && !talkAddDTO.getPictures().isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(talkAddDTO.getPictures());
                talk.setPictures(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        talkMapper.insert(talk);
    }

    @Override
    public TalkDetailVO getById(Long id) {
        Talk talk = talkMapper.selectById(id);
        TalkDetailVO vo = new TalkDetailVO();
        BeanUtils.copyProperties(talk, vo);

        if (talk.getPictures() != null && !talk.getPictures().isEmpty()) {
            try {
                List<String> pics = objectMapper.readValue(
                        talk.getPictures(),
                        new TypeReference<List<String>>() {}
                );
                vo.setPictures(pics);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 删除该杂谈的所有点赞记录
        likeRecordMapper.delete(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getTalkId, id)
        );

        // 删除该杂谈的所有评论
        commentMapper.delete(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getTalkId, id)
        );

        // 删除杂谈本身
        talkMapper.deleteById(id);
    }
}