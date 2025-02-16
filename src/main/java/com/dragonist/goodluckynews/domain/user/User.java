package com.dragonist.goodluckynews.domain.user;

import com.dragonist.goodluckynews.domain.common.AMPM;
import com.dragonist.goodluckynews.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//user
@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")  // 테이블 이름 변경
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AMPM ampm;

    @Column(nullable = false)
    private int hour;

    @Column(nullable = false)
    private int minute;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginPlatform loginPlatform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Builder
    public User(String name, String email, String profileImage, AMPM ampm, int hour, int minute,
                LoginPlatform loginPlatform, Role role) {
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.ampm = ampm;
        this.hour = hour;
        this.minute = minute;
        this.loginPlatform = loginPlatform;
        this.role = role;
    }

    public User update(String name, String profileImage) {
        this.name = name;
        this.profileImage = profileImage;
        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
