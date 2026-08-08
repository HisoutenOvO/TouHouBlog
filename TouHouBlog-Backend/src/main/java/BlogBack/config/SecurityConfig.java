package BlogBack.config;

import BlogBack.filter.JwtAuthFilter;
import BlogBack.pojo.entity.User;
import BlogBack.service.impl.CustomOAuth2UserService;
import BlogBack.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtUtils jwtUtils;
    private final JwtAuthFilter jwtAuthFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 暂时开放所有请求，后续再加权限控制
                        .requestMatchers("/**").permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler((request, response, authentication) -> {
                            // 登录成功后，获取 User 对象，生成 JWT
                            User user = (User) authentication.getPrincipal();
                            String token = jwtUtils.generateToken(user);
                            // 重定向到前端的登录成功页面，携带 token
                            response.sendRedirect("http://localhost:4321/login-success?token=" + token);
                        })
                )
                .csrf(csrf -> csrf.disable()); // 暂时关闭 CSRF，后续可根据需要开启
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}