package BlogBack.service.impl;

import BlogBack.mapper.UserMapper;
import BlogBack.pojo.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;

    @Value("${admin.gitee.id:0}")
    private Long adminGiteeId;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 先调用父类获取 OAuth2User 信息
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("gitee".equals(registrationId)) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Long giteeId = ((Number) attributes.get("id")).longValue();
            String login = (String) attributes.get("login");
            String name = (String) attributes.get("name");
            String avatarUrl = (String) attributes.get("avatar_url");

            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getGiteeId, giteeId));

            if (user == null) {
                user = new User();
                user.setGiteeId(giteeId);
                user.setUsername(login);
                user.setAvatarUrl(avatarUrl);
                // 管理员设置固定昵称，普通用户使用 Gitee 昵称
                if (giteeId.equals(adminGiteeId)) {
                    user.setNickname("Hisouten");   // 可改为你想要的任何昵称
                    user.setRole(1);
                } else {
                    user.setNickname(name != null ? name : login);
                    user.setRole(0);
                }
                userMapper.insert(user);
            } else {
                // 已有用户：管理员不更新昵称，普通用户可更新
                if (!giteeId.equals(adminGiteeId)) {
                    user.setNickname(name != null ? name : login);
                }
                user.setAvatarUrl(avatarUrl);
                userMapper.updateById(user);
            }
            return user;
        }

        // 如果不是 gitee，返回原始的 OAuth2User
        return oAuth2User;
    }
}