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

    // title, content, keywords에서 searchQuery를 찾는 쿼리 추가
    @Query("SELECT a FROM ArticleEntity a WHERE (a.title LIKE %:searchQuery% OR a.content LIKE %:searchQuery% OR a.keywords LIKE %:searchQuery%) ORDER BY a.createdAt DESC")
    Page<ArticleEntity> searchArticles(@Param("searchQuery") String searchQuery, Pageable pageable);

    // 회원 ID로 랜덤으로 하나의 게시글 조회
    @Query(value = "SELECT * FROM article a WHERE a.user_id = :userId ORDER BY RAND() LIMIT 1", nativeQuery = true)
    ArticleEntity findRandomArticleByUserId(@Param("userId") Long userId);


}
