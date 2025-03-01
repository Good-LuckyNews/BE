package com.draconist.goodluckynews.domain.article.repository;

import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.entity.CompletedTime;
import com.draconist.goodluckynews.domain.article.entity.Heart;
import com.draconist.goodluckynews.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CompletedTimeRepository extends JpaRepository<CompletedTime, Long> {
    // 이미 완료되었는지 확인하는 메서드
    @Query("SELECT h FROM Heart h WHERE h.member = :member AND h.article = :article")
    Optional<Heart> findByMemberAndArticle(Member member, ArticleEntity article);

}
