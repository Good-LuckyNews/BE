package com.draconist.goodluckynews.domain.article.repository;

import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<ArticleEntity,Long> {
    // 회원 ID로 게시글 조회
    @Query("SELECT a FROM ArticleEntity a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    Page<ArticleEntity> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
