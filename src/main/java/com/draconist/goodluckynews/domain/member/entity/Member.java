package com.draconist.goodluckynews.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userId")
    private Long id;
    @Column(name="email",unique = true, nullable = false, length = 100)
    private String email;
    @Column(name="password", nullable = false)
    private String password;
    @Column(name="name", nullable = false)
    private String name;
    @Column(name="profileImage")
    private String profileImage; //이미지는 또 따로 처리
    @Column(name="ampm", nullable = false)
    private String amPm;
    @Column(name="hours", nullable = false)
    private Integer hours;
    @Column(name="minutes", nullable = false)
    private Integer minutes;
    @Column(nullable = false)
    private String role; // 사용자 권한 not null이므로 기본값없이 사용



    //====== 편의 메소드 ======//

    // 프로필 이미지 저장
    public void changeProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
