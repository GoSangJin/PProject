package com.example.pproject.Controller;

import com.example.pproject.Constant.SocialType;
import com.example.pproject.DTO.UserDTO;
import com.example.pproject.Service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OAuth2Contoller {
    private final UserService userService;

    @GetMapping("/oauth2/register")
    public String socialRegisterForm(Model model, HttpSession session) {
        Map<String, Object> attributes = (Map<String, Object>) session.getAttribute("oauth2User");
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail((String) attributes.get("email"));
        userDTO.setUsername((String) attributes.get("username"));
        userDTO.setSocialType(SocialType.google);
        model.addAttribute("data", userDTO);
        return "User/SocialRegister";
    }

    @PostMapping("/oauth2/register")
    public String socialRegister(UserDTO userDTO, HttpSession session) {
        userDTO.setSocialType(SocialType.google); // 자동 설정
        userService.register(userDTO);
        session.removeAttribute("oauth2User");
        return "redirect:/";
    }
}
