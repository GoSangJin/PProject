package com.example.pproject.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.example.pproject.Entity.UserEntity;            // ★ 추가
import com.example.pproject.Repository.UserRepository;     // ★ 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository; // ★ 주입

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();
        Object principal = authentication.getPrincipal();

        String displayName;
        if (principal instanceof OAuth2User oauth2User) {
            // 소셜 로그인: OAuth2User의 "name" 속성 사용
            displayName = oauth2User.getAttribute("name");
        } else if (principal instanceof UserDetails userDetails) {
            // 일반 로그인: UserDetails.username은 로그인 아이디이므로,
            // DB에서 실제 'username' 필드(실명)를 조회
            String loginId = userDetails.getUsername();
            Optional<UserEntity> ue = userRepository.findByUserid(loginId);
            displayName = ue.map(UserEntity::getUsername).orElse(loginId);
        } else {
            displayName = principal.toString();
        }

        session.setAttribute("username", displayName);
        response.sendRedirect("/");
    }
}
