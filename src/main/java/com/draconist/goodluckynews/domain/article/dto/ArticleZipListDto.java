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
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime originalDate;
        private boolean bookmarked;
        private Integer degree;
        //완료버튼 누른 시간
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime completedTime;
}
