package com.draconist.goodluckynews.domain.article.dto;

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
    private LocalDateTime completedTime;
    private String originalLink;
    private String longContent;
    private String image; // 뉴스 이미지 하나
    private String keywords;
    private Long userId;
}
