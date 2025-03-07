package com.draconist.goodluckynews.domain.article.repository;

import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.entity.Heart;
import com.draconist.goodluckynews.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface HeartRepository extends JpaRepository<Heart, Long> {

    // 이미 좋아요가 있는지 확인하는 메서드
    @Query("SELECT h FROM Heart h WHERE h.member = :member AND h.article = :article")
    Optional<Heart> findByMemberAndArticle(Member member, ArticleEntity article);

    // 좋아요 수 증가
    @Modifying
    @Query("UPDATE ArticleEntity a SET a.likeCount = a.likeCount + 1 WHERE a.id = :articleId AND a.userId = :userId")
    void incrementLikeCount(@Param("articleId") Long articleId, @Param("userId") Long userId);

    // 좋아요 수 감소
    @Modifying
    @Query("UPDATE ArticleEntity a SET a.likeCount = a.likeCount - 1 WHERE a.id = :articleId AND a.userId = :userId")
    void decrementLikeCount(@Param("articleId") Long articleId, @Param("userId") Long userId);

    //사용자가 북마크한 기사보기
    @Query("SELECT h.article FROM Heart h WHERE h.member.id = :userId AND h.bookmarked = true")
    List<ArticleEntity> findBookmarkedArticlesByUserId(@Param("userId") Long userId);

    Optional<Heart> findByMemberIdAndArticleId(Long userId, Long id);
}
