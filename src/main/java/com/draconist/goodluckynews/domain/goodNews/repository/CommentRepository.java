package com.draconist.goodluckynews.domain.goodNews.repository;

import com.draconist.goodluckynews.domain.goodNews.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    List<Comment> findByUserId(Long userId);
    Page<Comment> findAll(Pageable pageable); // 페이지네이션을 적용한 전체 댓글 조회
    int countByPostId(Long id);//댓글 수
    void deleteByPostId(Long postId); // 희소식 삭제 연관
    void deleteById(Long commentId);// 댓글 삭제
    List<Comment> findByParentCommentId(Long parentCommentId);// 부모 댓글 ID를 통해 답글 목록 조회
    public List<Comment> findByParentComment(Comment parentComment);
    // 부모 댓글이 특정 사용자의 댓글인 대댓글 조회
}

