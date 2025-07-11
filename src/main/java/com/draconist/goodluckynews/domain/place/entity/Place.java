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


    // ✅ 북마크 상태 저장 (true: 북마크됨, false: 북마크 안됨)
    @Column(name = "isBookmarked", nullable = false)
    private boolean isBookmarked = false;


    public void changeUserId(Member member) {
        this.userId = member.getId();
    }


    // ✅ 플레이스 정보 수정 메서드 추가
    public void updatePlace(String placeName, String placeDetails, String placeImg) {
        this.placeName = placeName;
        this.placeDetails = placeDetails;
        if (placeImg != null) { // 이미지가 새로 제공된 경우만 업데이트
            this.placeImg = placeImg;
        }
    }


    // ✅ 북마크 토글 기능 추가
    public void toggleBookmark() {
        this.isBookmarked = !this.isBookmarked;
    }
}
