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
    public class CompletedTime extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "completedTimeId")
        private Long id;

        @ManyToOne(fetch = LAZY)
        @JoinColumn(name = "userId")
        private Member member;

        @ManyToOne(fetch = LAZY)
        @JoinColumn(name = "articleId")
        private ArticleEntity article;

        @Column(name = "completedAt")
        private LocalDateTime completedAt;

        @CreatedDate  // ✅ 생성 시 자동 저장
        @Column(updatable = false, nullable = false)
        private LocalDateTime createdAt;

        @LastModifiedDate  // ✅ 업데이트 시 자동 갱신
        @Column(nullable = false)
        private LocalDateTime updatedAt;

        public CompletedTime(Member member, ArticleEntity article, LocalDateTime completedAt) {
            this.member = member;
            this.article = article;
            this.completedAt = completedAt;
        }
        @Override
        public String toString() {
            return "CompletedTime{" +
                    "id=" + id +
                    ", memberId=" + (member != null ? member.getId() : "null") +
                    ", articleId=" + (article != null ? article.getId() : "null") +
                    ", completedAt=" + completedAt +
                    '}';
        }

    }
