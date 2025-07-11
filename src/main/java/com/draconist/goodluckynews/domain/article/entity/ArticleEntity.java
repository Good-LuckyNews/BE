package com.draconist.goodluckynews.domain.article.entity;

import com.draconist.goodluckynews.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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
    @Column(name="originalDate")
    private LocalDateTime originalDate;

    @Column(name="originalLink")
    private String originalLink;
    @Lob //이미지 TEXT 테이블에 저장
    @Column(name="image",columnDefinition = "TEXT")
    private String image; //이미지는 또 따로 처리
    @Column(name="longContent",columnDefinition = "TEXT")
    private String longContent;

    //userId에서 가져와 엔티티화
    @Column(name="keywords")
    private String keywords;

    @ColumnDefault("0")
    @Column(name = "likeCount")
    private Integer likeCount;

    @CreatedDate  // ✅ 생성 시 자동 저장
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate  // ✅ 업데이트 시 자동 갱신
    @Column(nullable = false)
    private LocalDateTime updatedAt;

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


}
