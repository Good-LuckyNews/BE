package com.draconist.goodluckynews.domain.goodNews.repository;

import com.draconist.goodluckynews.domain.goodNews.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);
    int countByCommentId(Long commentId); // 특정 댓글의 좋아요 개수 조회
    void deleteByCommentId(Long commentId);// 댓글 좋아요 삭제
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

}
