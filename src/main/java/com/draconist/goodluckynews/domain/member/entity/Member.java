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
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column
    private String profileImage; //이미지는 또 따로 처리

    @Column(nullable = false)
    private String amPm;

    @Column(nullable = false)
    private Integer hours;

    @Column(nullable = false)
    private Integer minutes;

    @Column(nullable = false)
    private String role; // 사용자 권한 not null이므로 기본값없이 사용
}
