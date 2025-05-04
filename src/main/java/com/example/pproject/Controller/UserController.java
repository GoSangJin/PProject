package com.example.pproject.Controller;

import com.example.pproject.Constant.RoleType;
import com.example.pproject.Constant.SocialType;
import com.example.pproject.DTO.UserDTO;
import com.example.pproject.Service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.IllformedLocaleException;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class UserController {
    private final UserService userService;

    // 로그인 페이지 이동
    @GetMapping("/Login")
    public String login() {
        return "User/Login";
    }

    // 로그아웃 하기 (섹션에 값 제거)
    @GetMapping("/Logout")
    public String logout(HttpSession session) {
        session.invalidate();                                       // 접속 컴퓨터의 정보가 서버에 존재하면 제거.
        return "redirect:/Login";
    }


    @GetMapping("/User/Register")
    public String registerForm(Model model) {
        model.addAttribute("data", new UserDTO());
        return "User/Register";
    }

    @PostMapping("/User/Register")
    public String registerProc(UserDTO userDTO, Model model) {
        try {
            // 서비스에서 회원가입 처리
            userService.register(userDTO);
        } catch (IllegalStateException e) {
            // 회원가입 실패 시 오류 메시지 추가
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("data", userDTO); // 사용자가 입력한 데이터를 다시 전달
            return "User/Register"; // 다시 회원가입 페이지로 이동
        }

        return "/User/Login"; // 회원가입 성공 시 메인 페이지로 리다이렉트
    }

    @GetMapping("/User/First_Social_Login")
    public String firstSocialLoginForm(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<String, Object> oauth2User = (Map<String, Object>) session.getAttribute("oauth2User");

        UserDTO data = new UserDTO();
        data.setUsername((String) oauth2User.get("name"));    // line 30
        data.setEmail((String) oauth2User.get("email"));      // line 31
        // ★ 자동 설정을 위해 아래 두 줄을 추가하세요 ( 바로 위 email 설정 다음에 )
        data.setRoleType(RoleType.user);                      // line 32
        data.setSocialType(SocialType.google);                // line 33

        model.addAttribute("data", data);
        return "User/First_Social_Login";
    }

    @PostMapping("/User/First_Social_Login")
    public String firstSocialLoginSubmit(@ModelAttribute("data") UserDTO userDTO, HttpSession session) {
        // ★ 신규 소셜 유저 등록: birthday, postcode, address, detailAddress, extraAddress만 채워져 있고
        //    username, email, roleType, socialType은 Hidden 필드로 이미 채워져 있음
        userService.register(userDTO);
        session.removeAttribute("oauth2User");
        return "redirect:/";
    }


}

