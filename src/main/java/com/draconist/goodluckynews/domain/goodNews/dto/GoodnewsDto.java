package com.draconist.goodluckynews.domain.goodNews.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodnewsDto {
    private String title; // 게시글 제목
    private Long placeId;      // 게시글이 속한 장소 ID
    private String content;    // 게시글 내용
    private String image;      // 이미지 URL (선택 사항)
}
