package com.draconist.goodluckynews.domain.goodNews.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private Long postId;       // 게시글 ID (조회 시 사용)
    private String title;      // 게시글 제목 (검색 및 조회 시 사용)
    private Long placeId;      // 게시글이 속한 장소 ID
    private Long userId;       // 게시글 작성자 ID
    private String content;    // 게시글 내용
    private String image;      // 이미지 URL (선택 사항)
    private LocalDateTime createdAt; // 생성 날짜
    private LocalDateTime updatedAt; // 수정 날짜
}