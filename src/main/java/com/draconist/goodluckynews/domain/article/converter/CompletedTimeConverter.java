package com.draconist.goodluckynews.domain.article.converter;

import com.draconist.goodluckynews.domain.article.dto.ArticleLongContentDto;
import com.draconist.goodluckynews.domain.article.dto.CompletedDegreeDto;
import com.draconist.goodluckynews.domain.article.dto.SevenCompletedGraphDto;
import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.entity.Heart;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CompletedTimeConverter {

    public ArticleLongContentDto toArticleLongContentDto(
            ArticleEntity article,
            Heart heart,
            CompletedDegreeDto completedDegreeDto) {
        boolean isBookmarked = heart != null && heart.isBookmarked();

        return ArticleLongContentDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .longContent(article.getLongContent())
                .originalLink(article.getOriginalLink())
                .image(article.getImage())
                .keywords(article.getKeywords())
                .degree(completedDegreeDto != null ? completedDegreeDto.getDegree() : null)
                .completedTime(completedDegreeDto != null ? completedDegreeDto.getCompletedTime() : null)
                .originalDate(article.getOriginalDate())
                .likeCount(article.getLikeCount())
                .bookmarked(isBookmarked)
                .build();
    }
    // 배열로 받아 패딩하여 SevenCompletedGraphDto로 만들어줌
    public SevenCompletedGraphDto toSevenCompletedGraphDto(
            Integer[] values,
            LocalDateTime firstCompletedAt,
            LocalDateTime lastCompletedAt
    ) {
        Integer[] padded = new Integer[7];
        for (int i = 0; i < 7; i++) {
            // 값이 있으면 넣고, 없으면 null
            padded[i] = (values != null && i < values.length) ? values[i] : null;
        }
        return SevenCompletedGraphDto.builder()
                .first(padded[0])
                .second(padded[1])
                .third(padded[2])
                .fourth(padded[3])
                .fifth(padded[4])
                .sixth(padded[5])
                .seventh(padded[6])
                .firstCompletedAt(firstCompletedAt)
                .lastCompletedAt(lastCompletedAt)
                .build();
    }


    // (기존: 날짜 정보 필요 없을 땐 null로)
    public SevenCompletedGraphDto toSevenCompletedGraphDto(Integer[] values) {
        return toSevenCompletedGraphDto(values, null, null);
    }
}
