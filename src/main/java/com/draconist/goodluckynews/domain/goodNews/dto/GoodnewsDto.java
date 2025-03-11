package com.draconist.goodluckynews.domain.goodNews.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
public class GoodnewsDto {
    @NotNull(message = "Title is required")
    private String title; // 게시글 제목
    private Long placeId;      // 게시글이 속한 장소 ID
    private String content;    // 게시글 내용
}
