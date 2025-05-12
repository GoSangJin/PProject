package com.example.pproject.Service;

import com.example.pproject.Constant.RoleType;
import com.example.pproject.Constant.SocialType;
import com.example.pproject.DTO.UserDTO;
import com.example.pproject.Entity.UserEntity;
import com.example.pproject.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));

        log.info("{} 사용자 로그인 시도", userEntity);

        return User.builder()
                .username(userEntity.getUserid()) // ✅ 로그인 아이디로 사용
                .password(userEntity.getPassword()) // ✅ 암호화된 비밀번호
                .roles(userEntity.getRoleType().name()) // ROLE_ 접두어 자동 부여됨
                .build();
    }

    public void register(UserDTO userDTO) {
        // — 일반 회원일 경우에만 userid 중복 체크
        if (userDTO.getUserid() != null && !userDTO.getUserid().isEmpty()) {
            Optional<UserEntity> existingUser = userRepository.findByUserid(userDTO.getUserid());
            if (existingUser.isPresent()) {
                throw new IllegalStateException("이미 존재하는 회원입니다.");
            }
        }
        // 이메일 중복은 항상 체크
        Optional<UserEntity> existingEmail = userRepository.findByEmail(userDTO.getEmail());
        if (existingEmail.isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);

        // 비밀번호가 있으면 암호화, 없으면 null (소셜 유저용)
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            userEntity.setPassword(null);
        }

        userEntity.setRoleType(RoleType.user);

        // ★ 소셜 최초 가입: DTO에 담긴 socialType을 우선 사용
        if (userDTO.getSocialType() != null) {
            userEntity.setSocialType(userDTO.getSocialType());
        } else {
            // 기존 도메인 기반 로직 보강 (gmail.com 처리 추가)
            String email = userDTO.getEmail().toLowerCase();
            String domain = email.contains("@") ? email.split("@")[1] : "";
            if (domain.startsWith("naver")) {
                userEntity.setSocialType(SocialType.naver);
            } else if (domain.startsWith("google") || domain.startsWith("gmail")) {
                userEntity.setSocialType(SocialType.google);
            } else if (domain.startsWith("kakao")) {
                userEntity.setSocialType(SocialType.kakao);
            } else if (domain.startsWith("nate")) {
                userEntity.setSocialType(SocialType.nate);
            }
            else {
                userEntity.setSocialType(SocialType.Else);
            }
        }

        userRepository.save(userEntity);
    }

    // 이메일,생일,이름으로 아이디 찾기
    public String findUseridByEmailAndBirthdayAndUsername(String email, String birthday, String username) {
        UserEntity userEntity = userRepository.findByEmailAndBirthdayAndUsername(email, birthday, username);
        return userEntity != null ? userEntity.getUserid() : null;
    }


    // 비밀번호 검증 메서드
    public boolean verifyPassword(String userid, String password) {
        UserEntity userEntity = userRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userid));

        // 저장된 비밀번호와 입력된 비밀번호 비교
        return passwordEncoder.matches(password, userEntity.getPassword());
    }

    // 비밀번호 업데이트 메서드
    public void updatePassword(String userid, String newPassword) {
        UserEntity userEntity = userRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userid));

        userEntity.setPassword(passwordEncoder.encode(newPassword)); // 비밀번호 해싱
        userRepository.save(userEntity);
    }

    public String findEmailByUseridAndUsernameAndBirthday(String userid, String username, String birthday) {
        UserEntity userEntity = userRepository.findByUseridAndUsernameAndBirthday(userid, username, birthday)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return userEntity.getEmail();
    }
}
