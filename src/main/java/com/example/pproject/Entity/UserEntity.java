package com.example.pproject.Entity;

import com.example.pproject.Constant.RoleType;
import com.example.pproject.Constant.SocialType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table(name=" user")
@Entity
public class UserEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String userid;
    private String password;
    @Column(unique = true)
    private String email;
    private String birthday;
    private String username;
    private String postcode;
    private String address;
    private String detailAddress;
    private String extraAddress;
    @Enumerated(EnumType.STRING)
    private RoleType roleType;
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private boolean isSuspended;
    private LocalDate suspensionEndDate;
}
