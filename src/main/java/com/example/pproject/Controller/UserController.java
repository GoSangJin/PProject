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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.IllformedLocaleException;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class UserController {
    private final UserService userService;


    // 로그인 페이지 이동
    @GetMapping("/Login")
    public String login(Model model, HttpSession session) {
        // (1) 세션에서 에러 메시지 가져오기
        String msg = (String) session.getAttribute("loginErrorMessage");
        if (msg != null) {
            model.addAttribute("errorMessage", msg);
            session.removeAttribute("loginErrorMessage");
        }
        return "User/Login";
    }

    // 로그아웃 하기 (섹션에 값 제거)
    @GetMapping("/Logout")
    public String logout(HttpSession session) {
        session.invalidate();                                       // 접속 컴퓨터의 정보가 서버에 존재하면 제거.
        return "redirect:/Login";
    }


    @GetMapping("/User/Register") // 회원가입 페이지
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

        return "/User/Login"; // 회원가입 성공 시 메인 페이지로
    }


    @GetMapping("/User/First_Social_Login") // 최초 소셜 로그인 이용자 정보 입력 페이지
    public String firstSocialLoginForm(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<String, Object> oauth2User = (Map<String, Object>) session.getAttribute("oauth2User");

        UserDTO data = new UserDTO();
        data.setUsername((String) oauth2User.get("name"));
        data.setEmail((String) oauth2User.get("email"));
        data.setRoleType(RoleType.user);
        data.setSocialType(SocialType.google);

        model.addAttribute("data", data);
        return "User/First_Social_Login";
    }

    @PostMapping("/User/First_Social_Login")
    public String firstSocialLoginSubmit(@ModelAttribute("data") UserDTO userDTO, HttpSession session) {
        userService.register(userDTO);
        session.removeAttribute("oauth2User");
        return "redirect:/";
    }


    @GetMapping("/User/Find_Userid") // 아이디 찾기 페이지
    public String findUsernameForm() {
        return "User/Find_Userid";
    }

    @PostMapping("/User/Find_Userid") // 아이디 찾기 요청 처리
    public String findUsername(@RequestParam String email,
                               @RequestParam String birthday,
                               @RequestParam String username,
                               Model model) {
        String userid = userService.findUseridByEmailAndBirthdayAndUsername(email, birthday, username);
        if (userid != null) {
            model.addAttribute("message", "당신의 아이디는: " + userid);
        } else {
            model.addAttribute("message", "정보와 일치하는 계정이 없습니다.");
        }
        return "User/Result_Userid"; // 결과 페이지
    }


    @GetMapping("/User/Change_Password")
    public String showChangePasswordPage() {return "User/Change_Password";}

    @PostMapping("/User/Change_Password")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 Model model) {
        String userid = (String) session.getAttribute("userid");

        if (userid == null) {
            return "redirect:/Login";
        }

        if (!userService.verifyPassword(userid, currentPassword)) {
            model.addAttribute("errorMessage","현재 비밀번호가 일치하지 않습니다.");
            return "User/Change_Password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage","새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "User/Change_Password";
        }

        userService.updatePassword(userid, newPassword);
        return "redirect:/";
    }


}


