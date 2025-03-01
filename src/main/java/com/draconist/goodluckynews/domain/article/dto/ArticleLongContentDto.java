package com.draconist.goodluckynews.domain.article.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Data
@Getter
@Setter
@Builder
public class ArticleLongContentDto {
    @NotNull
    private Long id;
    @NotNull
    private String title;
    @NotNull
    private String longContent;
    @NotNull
    private String originalLink;
    private String image; // 뉴스 이미지 하나
    @NotNull
    private String keywords;
    //생성시간
    private LocalDateTime createdAt;

    private Integer degree;
    //완료버튼 누른 시간
    private LocalDateTime completedTime;
    private Integer likeCount;

}
