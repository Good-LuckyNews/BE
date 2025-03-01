package com.draconist.goodluckynews.domain.article.entity;

import com.draconist.goodluckynews.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "article")
public class ArticleEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="articleId")
    private Long id;

    @Column(name="userId", nullable = false)
    private Long userId;

    @Column(name="title", nullable = false)
    private String title;
    @Column(name="content", nullable = false)
    private String content;

    @Column(name="originalLink")
    private String originalLink;
    @Lob //이미지 TEXT 테이블에 저장
    @Column(name="image",columnDefinition = "TEXT")
    private String image; //이미지는 또 따로 처리
    @Column(name="longContent",columnDefinition = "TEXT")
    private String longContent;


    @Column(name="degree")
    private Integer degree;
    @Column(name="completedTime")
    private LocalDateTime completedTime;

    //userId에서 가져와 엔티티화
    @Column(name="keywords")
    private String keywords;

    @ColumnDefault("0")
    @Column(name = "likeCount")
    private Integer likeCount;

    public void updateLikeCount(boolean increase) {
        if (increase) {
            this.likeCount++;
        } else {
            this.likeCount--;
        }
    }
    // @PrePersist 메서드 추가 기사 저장시 좋아요 저장안하면 null->0으로
    @PrePersist
    public void prePersist() {
        if (this.likeCount == null) {
            this.likeCount = 0;
        }
    }

    //완료시간 기록
    public void updateCompletedTime(LocalDateTime completedNowTime) {
        this.completedTime = completedNowTime;
    }

    //긍정도 기록
    public void updateDegree(Integer degree) {
        this.degree = degree;
    }

}
