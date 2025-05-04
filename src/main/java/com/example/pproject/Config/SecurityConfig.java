package com.example.pproject.Config;

import com.example.pproject.Service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1) URL 접근 권한 설정
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/test1", "/test2").permitAll();
            auth.requestMatchers(
                    "/user/update", "/board/new", "/board/edit", "/board/delete",
                    "/board/like", "/comment/insert", "/user/info", "/User/MyPage",
                    "/user/other_user_page", "/user/change_password", "/user/re_enter_credentials"
            ).authenticated();
            auth.requestMatchers("/User/Register", "/user/find-userid", "/user/find-password").anonymous();
            auth.requestMatchers("/images/**").permitAll();
            auth.requestMatchers(
                    "/product/new", "/product/edit", "/product/delete",
                    "/qna/insert", "/qna/delete/{id}", "/qna/update/{id}",
                    "/document/insert", "/document/update/", "/plantation/delete",
                    "/plantation/insert", "/plantation/update",
                    "/event/delete", "/event/create", "/event/update",
                    "/admin/**", "/fruit/create", "/fruit/delete", "/fruit/update",
                    "/disease/create", "/disease/delete", "/disease/update"
            ).hasAnyRole("admin","master");
            auth.anyRequest().permitAll();
        });

        // 2) 폼 로그인 설정
        http.formLogin(login -> login
                .loginPage("/Login")
                .loginProcessingUrl("/Login")
                .usernameParameter("userid")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .permitAll()
                .successHandler(customAuthenticationSuccessHandler)
        );

        // 3) 소셜 로그인 설정 (수정된 부분)
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/Login")
                .userInfoEndpoint(userInfo ->
                        userInfo.userService(customOAuth2UserService)
                )
                .successHandler(customAuthenticationSuccessHandler)        // 폼 로그인과 동일 핸들러 사용
                .failureHandler((request, response, exception) -> {
                    // user_not_registered 에러는 회원가입 페이지로
                    if (exception instanceof OAuth2AuthenticationException oae
                            && "user_not_registered".equals(oae.getError().getErrorCode())) {
                        response.sendRedirect("/User/First_Social_Login");
                    } else {
                        response.sendRedirect("/Login?error");
                    }
                })
        );

        // 4) (기존) 인증 진입점 처리: 로그인 안 된 상태로 보호된 URL 접근 시
        http.exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/Login");
                })
        );

        // 5) 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/Logout")
                .logoutSuccessUrl("/Login")
        );

        // 6) CSRF 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
