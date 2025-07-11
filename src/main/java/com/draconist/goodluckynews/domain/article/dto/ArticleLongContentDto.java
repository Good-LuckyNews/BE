package com.draconist.goodluckynews.domain.article.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Integer degree;
    //완료버튼 누른 시간
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedTime;
    private Integer likeCount;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime originalDate;
    private boolean bookmarked;
}
