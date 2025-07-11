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
public class ArticleDto {
    @NotNull
    private String title;
    @NotNull
    private String content;
    private String longContent;
    private String originalLink;
    private String image; //이미지는 또 따로 처리
    private String keywords;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime originalDate;
    private boolean bookmarked;
}
