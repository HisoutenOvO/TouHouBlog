package BlogBack.service.impl;

import BlogBack.mapper.LikeRecordMapper;
import BlogBack.pojo.entity.LikeRecord;
import BlogBack.pojo.vo.LikeVO;
import BlogBack.service.TalkLikeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TalkLikeServiceImpl implements TalkLikeService {
    private final LikeRecordMapper likeRecordMapper;

    @Override
    @Transactional
    public LikeVO toggleLike(Long talkId, Long userId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getTalkId, talkId)
                .eq(LikeRecord::getUserId, userId);
        LikeRecord record = likeRecordMapper.selectOne(wrapper);
        if (record != null) {
            likeRecordMapper.deleteById(record.getId());
        } else {
            LikeRecord newRecord = new LikeRecord();
            newRecord.setTalkId(talkId);
            newRecord.setUserId(userId);
            likeRecordMapper.insert(newRecord);
        }
        long count = likeRecordMapper.selectCount(
                new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getTalkId, talkId));
        return new LikeVO(count, record == null);
    }

    @Override
    public LikeVO getLikeStatus(Long talkId, Long userId) {
        long count = likeRecordMapper.selectCount(
                new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getTalkId, talkId));
        boolean liked = likeRecordMapper.exists(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getTalkId, talkId)
                        .eq(LikeRecord::getUserId, userId));
        return new LikeVO(count, liked);
    }
}