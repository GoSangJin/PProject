package com.example.pproject.Service;

import com.example.pproject.Entity.UserEntity;
import com.example.pproject.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = new DefaultOAuth2UserService().loadUser(request);
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");

        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            // 사용자 정보가 DB에 없으면 세션에 임시 저장 후 사용자 정보 입력 페이지로 리다이렉트 유도
            session.setAttribute("oauth2User", user.getAttributes());
            throw new OAuth2AuthenticationException(new OAuth2Error("user_not_registered"), "New social user");
        }

        return user;
    }
}
