package BlogBack.service.impl;

import BlogBack.mapper.UserMapper;
import BlogBack.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
}
