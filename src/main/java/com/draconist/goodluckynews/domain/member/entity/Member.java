package com.draconist.goodluckynews.domain.member.entity;

import com.draconist.goodluckynews.domain.member.dto.MemberInfoDTO;
import com.draconist.goodluckynews.domain.place.entity.Place;
import com.draconist.goodluckynews.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Member")
public class Member extends BaseEntity {
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
    @Column(name="amPm", nullable = false)
    private String amPm;
    @Column(name="hours", nullable = false)
    private Integer hours;
    @Column(name="minutes", nullable = false)
    private Integer minutes;
    @Column(nullable = false)
    private String role; // 사용자 권한 not null이므로 기본값없이 사용
    @Lob //이미지 TEXT 테이블에 저장
    @Column(name="profileImage",columnDefinition = "TEXT")
    private String profileImage; //이미지는 또 따로 처리

    @Column(name="keywords")
    private String keywords;



    //====== 연관 매핑 ======//

    @Builder.Default
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    private List<Place> myPlaces = new ArrayList<>();

    //커뮤니티 생성
    public void createPlace(Place place) {
        // 1. 만든 사람 아이디를 플레이스 엔티티에 저장
        place.changeUserId(this);

        // 2. 내가 만든 커뮤니티 목록에 저장
        this.myPlaces.add(place);
    }

    //====== 편의 메소드 ======//

    // 프로필 이미지 저장
    public void changeProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // 회원 정보 수정 메소드
    public void changeUserInfo(MemberInfoDTO userInfoDTO) {
        if(userInfoDTO.getName()!=null) {
            this.name = userInfoDTO.getName();
        }
        if(userInfoDTO.getAmPm()!=null) {
            this.amPm= userInfoDTO.getAmPm();
        }
        if(userInfoDTO.getHours()!=null) {
            this.hours= userInfoDTO.getHours();
        }
        if(userInfoDTO.getMinutes()!=null) {
            this.minutes= userInfoDTO.getMinutes();
        }
        if(userInfoDTO.getKeywords() != null) {  // keywords 업데이트
            this.keywords = userInfoDTO.getKeywords();
        }
    }
}
