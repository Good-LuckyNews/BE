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
public class ArticleZipListDto {
        @NotNull
        private Long id;
        @NotNull
        private String title;
        @NotNull
        private String content;
        private String image;
        private String keywords;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        private Integer likeCount;
}
