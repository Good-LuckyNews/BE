package com.draconist.goodluckynews.domain.article.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeartDto {

    private Long memberId;
    private Long articleId;
    private boolean bookmarked;

    public HeartDto(Long memberId, Long articleId) {
        this.memberId = memberId;
        this.articleId = articleId;
        this.bookmarked = bookmarked;
    }
}
