package BlogBack.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Data
public class User implements OAuth2User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatarUrl;

    private Integer role;      // 0=用户, 1=管理员

    // 第三方登录绑定
    private Long giteeId;      // 必须存在！否则 CustomOAuth2UserService 报错

    // ---------- OAuth2User 接口实现 ----------
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + (role == 1 ? "ADMIN" : "USER"))
        );
    }

    @Override
    public String getName() {
        return this.id != null ? this.id.toString() : "";
    }
}