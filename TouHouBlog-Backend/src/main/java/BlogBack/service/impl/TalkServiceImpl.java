package BlogBack.service.impl;

import BlogBack.mapper.TalkMapper;
import BlogBack.service.TalkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TalkServiceImpl implements TalkService {
    private final TalkMapper talkMapper;
}
