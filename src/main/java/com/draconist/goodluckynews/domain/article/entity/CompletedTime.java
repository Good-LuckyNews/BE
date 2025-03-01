package com.draconist.goodluckynews.domain.article.entity;

import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

        @Column(name = "completedTime")
        private LocalDateTime completedAt;

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
