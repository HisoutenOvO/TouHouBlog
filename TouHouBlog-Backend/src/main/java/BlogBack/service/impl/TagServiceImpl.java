package BlogBack.service.impl;

import BlogBack.mapper.TagMapper;
import BlogBack.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagMapper tagMapper;
}
