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

    @Value("${admin.github.id:0}")
    private Long adminGithubId;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("gitee".equals(registrationId)) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Long giteeId = ((Number) attributes.get("id")).longValue();
            String login = (String) attributes.get("login");
            String name = (String) attributes.get("name");
            String avatarUrl = (String) attributes.get("avatar_url");
            String displayName = (name != null && !name.isEmpty()) ? name : login;

            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getGiteeId, giteeId));
            if (user == null) {
                user = new User();
                user.setGiteeId(giteeId);
                user.setUsername(login);
                user.setAvatarUrl(avatarUrl);
                if (giteeId.equals(adminGiteeId)) {
                    user.setNickname("Hisouten");
                    user.setRole(1);
                } else {
                    user.setNickname(displayName);
                    user.setRole(0);
                }
                userMapper.insert(user);
            } else {
                user.setAvatarUrl(avatarUrl);
                if (!giteeId.equals(adminGiteeId)) {
                    user.setNickname(displayName);
                }
                userMapper.updateById(user);
                // 对于已有用户，根据当前配置重新判断角色
                user.setRole(giteeId.equals(adminGiteeId) ? 1 : 0);
                userMapper.updateById(user);
            }
            return user;
        }
        else if ("github".equals(registrationId)) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            Long githubId = ((Number) attributes.get("id")).longValue();
            String login = (String) attributes.get("login");
            String avatarUrl = (String) attributes.get("avatar_url");
            String name = (String) attributes.get("name");
            String displayName = (name != null && !name.isEmpty()) ? name : login;

            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getGithubId, githubId));
            if (user == null) {
                user = new User();
                user.setGithubId(githubId);
                user.setUsername(login);
                user.setAvatarUrl(avatarUrl);
                if (githubId.equals(adminGithubId)) {
                    user.setNickname("Hisouten");
                    user.setRole(1);
                } else {
                    user.setNickname(displayName);
                    user.setRole(0);
                }
                userMapper.insert(user);
            } else {
                user.setAvatarUrl(avatarUrl);
                if (!githubId.equals(adminGithubId)) {
                    user.setNickname(displayName);
                }
                userMapper.updateById(user);
            }
            // 对于已有用户，根据当前配置重新判断角色
            user.setRole(githubId.equals(adminGithubId) ? 1 : 0);
            userMapper.updateById(user);
            return user;
        }

        return oAuth2User;
    }
}