package com.draconist.goodluckynews.domain.goodNews.controller;

import com.draconist.goodluckynews.domain.goodNews.dto.CommentDto;
import com.draconist.goodluckynews.domain.goodNews.service.CommentService;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments") // 포스트 별 댓글
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @PostMapping
    public ResponseEntity<?> createComment(
            @PathVariable Long postId,
            @RequestBody CommentDto commentDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.createComment(postId, commentDto, userDetails.getEmail());
    }

    // 포스트별 댓글 전체 조회
    @GetMapping
    public ResponseEntity<?> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return commentService.getCommentsByPost(postId, page, size);
    }

    // 대댓글 작성
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<?> createReplyToComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentDto commentDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.createReplyToComment(postId, commentDto, userDetails.getEmail(), commentId);
    }

    // 댓글 좋아요 토글
    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> toggleCommentLike(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.toggleCommentLike(postId, commentId, userDetails.getEmail());
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.deleteComment(postId, commentId, userDetails.getEmail());
    }

}
