package com.draconist.goodluckynews.domain.article.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ArticleListDto {
    private Long id;
    private String title;
    private String content;
    private Integer degree;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedTime;
    private String originalLink;
    private String longContent;
    private String image; // 뉴스 이미지 하나
    private String keywords;
    private Long userId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime originalDate;
    private Integer likeCount;
}
