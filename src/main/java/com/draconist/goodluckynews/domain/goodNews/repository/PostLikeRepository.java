package com.draconist.goodluckynews.domain.goodNews.repository;

import com.draconist.goodluckynews.domain.goodNews.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);
    int countByPostId(Long postId);   // 특정 게시글의 좋아요 개수 조회
    void deleteByPostId(Long postId);
}
