package com.draconist.goodluckynews.domain.goodNews.controller;


import com.draconist.goodluckynews.domain.goodNews.dto.PostDto;
import com.draconist.goodluckynews.domain.goodNews.service.PostService;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestBody PostDto postDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return postService.createPost(postDto, userDetails.getEmail());
    }//희소식 생성


    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        return postService.getPostById(postId);
    }//희소식 상세 조회

    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @RequestParam(defaultValue = "0") int page,  // 기본값 0
            @RequestParam(defaultValue = "10") int size // 기본값 10
    ) {
        return postService.getAllPosts(page, size);
    }//희소식 전체 조회 (페이지네이션)

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> togglePostLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return postService.togglePostLike(postId, userDetails.getEmail());
    }//좋아요

    @GetMapping("/mypage")
    public ResponseEntity<?> getMyPosts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return postService.getMyPosts(userDetails.getEmail());
    }//내가 쓴 페이지 조회

    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(@RequestParam String query) {
        return postService.searchPostsByTitle(query);
    }// 희소식 검색 (게시글 제목 기준으로)
}
