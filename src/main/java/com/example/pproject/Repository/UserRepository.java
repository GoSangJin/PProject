package com.example.pproject.Repository;

import com.example.pproject.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByUserid(String userid);
    Optional<UserEntity> findByEmail(String email);

// 비밀번호 찾기
    Optional<UserEntity> findByUseridAndUsernameAndBirthday(String userid, String username, String birthday);

    UserEntity findByEmailAndBirthdayAndUsername(String email, String birthday, String username);
}
