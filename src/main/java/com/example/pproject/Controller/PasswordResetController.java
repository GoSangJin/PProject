package com.example.pproject.Controller;

import com.example.pproject.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Random;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {
    private final UserService userService;
    private final JavaMailSender mailSender;
    private String tempEmail;
    private String tempCode;

    @GetMapping("/User/Find_Password")
    public String findPasswordForm() {
        return "User/Find_Password"; // 비밀번호 찾기 페이지
    }

    @PostMapping("/User/Find_Password")
    public String findPassword(@RequestParam String userid, @RequestParam String username, @RequestParam String birthday,
                               Model model) {
        // 아이디,성함,생일 로 사용자 확인
        String email = userService.findEmailByUseridAndUsernameAndBirthday(userid, username, birthday);

        if (email != null) {
            tempEmail = email;
            tempCode = generateRandomCode();
            sendVerificationEmail(tempEmail, tempCode);
            model.addAttribute("userid", userid);
            return "User/Verify_Code"; // 인증번호 입력 페이지
        }

        model.addAttribute("error", "정보가 일치하지 않습니다.");
        return "User/Find_Password"; // 다시 비밀번호 찾기 페이지
    }

    @PostMapping("/User/Verify_Code")
    public String verifyCode(@RequestParam String userid, @RequestParam String inputCode, Model model) {
        if (inputCode.equals(tempCode)) {
            model.addAttribute("userid", userid);
            return "User/New_Password"; // 비밀번호 변경 페이지
        }
        model.addAttribute("error", "인증번호가 일치하지 않습니다.");
        return "User/Verify_Code"; // 다시 인증번호 입력 페이지
    }

    @PostMapping("/User/New_Password")
    public String resetPassword(@RequestParam String userid,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Model model) {
        // (1) 입력값 일치 여부 확인
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("userid", userid);
            model.addAttribute("error", "비밀번호가 서로 다릅니다.");
            return "User/New_Password";
        }
        // (2) 일치하면 실제 업데이트
        userService.updatePassword(userid, newPassword);
        return "redirect:/Login";
    }

    private String generateRandomCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999)); // 6자리 랜덤 코드 생성
    }

    private void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("비밀번호 찾기 인증번호");
        message.setText("인증번호: " + code);
        mailSender.send(message);
    }
}
