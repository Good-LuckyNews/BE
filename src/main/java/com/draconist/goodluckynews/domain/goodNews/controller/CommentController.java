package com.draconist.goodluckynews.domain.goodNews.controller;

import com.draconist.goodluckynews.domain.goodNews.dto.CommentDto;
import com.draconist.goodluckynews.domain.goodNews.service.CommentService;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> createComment(
            @RequestBody CommentDto commentDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.createComment(commentDto, userDetails.getEmail());
    }

    @GetMapping("/mypage")  //
    public ResponseEntity<?> getMyComments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.getMyComments(userDetails.getEmail());
    }

    @GetMapping()
    public ResponseEntity<?> getAllComments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.getAllComments();
    }
    //댓글 전체조회

    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.toggleCommentLike(commentId, userDetails.getEmail());
    }//댓글 좋아요

    @PostMapping("/reply/{commentId}")
    public ResponseEntity<?> createReplyToComment(
            @PathVariable Long commentId,
            @RequestBody CommentDto commentDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.createReplyToComment(commentDto, userDetails.getEmail(),commentId);
    } //특정 댓글에 reply달기


    @GetMapping("/myalarm")
    public ResponseEntity<?> commentAlarm(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.commentAlarm(userDetails.getEmail());
    }//내 댓글에 달린 댓글

}
