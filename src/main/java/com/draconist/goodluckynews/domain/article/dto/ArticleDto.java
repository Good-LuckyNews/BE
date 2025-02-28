package com.draconist.goodluckynews.domain.article.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
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
public class ArticleDto {
    @NotNull
    private String title;
    @NotNull
    private String content;
    private String longContent;
    private String originalLink;
    private String image; //이미지는 또 따로 처리
    private String keywords;
    private LocalDateTime createdAt;
}
