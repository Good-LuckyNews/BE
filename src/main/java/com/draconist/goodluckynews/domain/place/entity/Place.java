package com.draconist.goodluckynews.domain.place.entity;

import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="Place")
public class Place extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="placeId")
    private Long id;
    @Column(name="userId", nullable = false)
    private Long userId;
    @Column(name="placeName", nullable = false)
    private String placeName;
    @Column(name="placeDetails", nullable = false)
    private String placeDetails;
    @Lob //이미지 TEXT 테이블에 저장
    @Column(name="placeImg",columnDefinition = "TEXT")
    private String placeImg;

    public void changeUserId(Member member) {
        this.userId = member.getId();
    }
}
