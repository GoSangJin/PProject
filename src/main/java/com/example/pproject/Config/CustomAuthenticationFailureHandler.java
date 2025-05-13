package com.example.pproject.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                                          HttpServletResponse response,
                                                          AuthenticationException exception)
                                                          throws IOException, ServletException {
        String errorMessage;

        if (exception instanceof UsernameNotFoundException) {
            errorMessage = "해당 아이디는 없는 계정입니다.";
        }
        else if (exception instanceof BadCredentialsException) {
            errorMessage = "비밀번호를 잘못 입력하셨습니다.";
        }
        else {
            errorMessage = "알 수 없는 이유로 로그인에 실패했습니다.";
        }
        HttpSession session = request.getSession();
        session.setAttribute("loginErrorMessage", errorMessage);
        response.sendRedirect("/Login");
    }
}
