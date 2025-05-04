package com.example.pproject.DTO;

import com.example.pproject.Constant.RoleType;
import com.example.pproject.Constant.SocialType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer id;                            // 일련번호
    private String userid;                      // 아이디
    private String password;                // 비밀번호
    private String email;                      // 이메일
    private String birthday;                // 생년 월일
    private String username; // 이름
    private String postcode; // 우편번호
    private String address; // 주소
    private String detailAddress; // 상세 주소
    private String extraAddress; // 참고 주소
    private RoleType roleType; // 권한
    private SocialType socialType; // 소셜 타입
    private LocalDateTime modDate; // 생성 날짜
}
