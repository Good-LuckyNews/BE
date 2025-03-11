package com.draconist.goodluckynews.domain.goodNews.repository;

import com.draconist.goodluckynews.domain.goodNews.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))") //내용 조회
    List<Post> searchByContent(@Param("query") String query);
    List<Post> findByUserId(Long userId); // 특정 사용자의 모든 게시글 조회
    // 🔹 여러 개의 게시글 ID를 한 번에 조회하는 기능 추가
    @Query("SELECT p FROM Post p JOIN FETCH Place pl ON p.placeId = pl.id WHERE p.userId = :userId")
    List<Post> findByUserIdWithPlace(@Param("userId") Long userId);
    List<Post> findByIdIn(Set<Long> postIds);
    void deleteById(Long postId);//삭제 기능 추가
}

