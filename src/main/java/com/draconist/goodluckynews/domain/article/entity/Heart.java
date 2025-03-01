package com.draconist.goodluckynews.domain.article.entity;

import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    public Heart(Member member, ArticleEntity article) {
        this.member = member;
        this.article = article;
    }
}
