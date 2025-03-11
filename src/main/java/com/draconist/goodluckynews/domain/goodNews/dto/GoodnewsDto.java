package com.draconist.goodluckynews.domain.goodNews.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
public class GoodnewsDto {
    private Long placeId;      // 게시글이 속한 장소 ID
    private String content;    // 게시글 내용
}//생성 전용 dto
