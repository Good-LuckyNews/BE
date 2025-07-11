package com.draconist.goodluckynews.domain.goodNews.controller;

import com.draconist.goodluckynews.domain.goodNews.dto.CommentDto;
import com.draconist.goodluckynews.domain.goodNews.service.CommentService;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/mypage/comments")
@RequiredArgsConstructor
public class MyPageCommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<?> getMyComments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return commentService.getMyComments(userDetails.getEmail());
    }
}// 내가 작성한 댓글 조회


