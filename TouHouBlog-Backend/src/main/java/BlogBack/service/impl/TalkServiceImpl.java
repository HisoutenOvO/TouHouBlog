package BlogBack.service.impl;

import BlogBack.common.result.PageResult;
import BlogBack.mapper.TalkMapper;
import BlogBack.pojo.dto.TalkAddDTO;
import BlogBack.pojo.dto.TalkPageQueryDTO;
import BlogBack.pojo.entity.Talk;
import BlogBack.pojo.vo.TalkDetailVO;
import BlogBack.service.TalkService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalkServiceImpl implements TalkService {

    private final TalkMapper talkMapper;
    private final ObjectMapper objectMapper;   // Spring 会自动注入

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
        // 处理多图：将 List 转成 JSON 字符串
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

        // 解析多图 JSON 字符串为 List
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
    public void delete(Long id) {
        talkMapper.deleteById(id);
    }
}