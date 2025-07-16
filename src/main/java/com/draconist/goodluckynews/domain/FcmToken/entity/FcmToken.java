package com.draconist.goodluckynews.domain.FcmToken.entity;

import com.draconist.goodluckynews.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Boolean active = true;

    // 필요하다면 생성일, 마지막 사용일 등 추가
}

