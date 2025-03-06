package com.draconist.goodluckynews.domain.article.entity;

import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Heart extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "heartId")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "userId")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "articleId")
    private ArticleEntity article;

    @Column(name = "bookmarked", nullable = false)
    private boolean bookmarked;  // 북마크 여부 추가

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Heart(Member member, ArticleEntity article, boolean bookmarked) {
        this.member = member;
        this.article = article;
        this.bookmarked = bookmarked;
    }

    @PrePersist
    public void prePersist() {
        this.bookmarked = false; // 기본값 설정
    }
    // 북마크 상태 변경 메서드
    //취소
    public void cancelBookmark() {
        this.bookmarked = false;
    }
    //북마크
    public void writeBookmark() {
        this.bookmarked = false;
    }
}
