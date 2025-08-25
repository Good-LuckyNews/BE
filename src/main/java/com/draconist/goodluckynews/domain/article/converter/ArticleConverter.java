package com.draconist.goodluckynews.domain.article.converter;

import com.draconist.goodluckynews.domain.article.dto.*;
import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.entity.Heart;
import com.draconist.goodluckynews.domain.article.repository.CompletedTimeRepository;
import com.draconist.goodluckynews.domain.article.repository.HeartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleConverter {

    private final HeartRepository heartRepository;
    private final CompletedTimeRepository completedTimeRepository;
    /** Dto → Entity 변환 */
    public ArticleEntity toEntity(ArticleDto dto, Long userId) {
        return ArticleEntity.builder()
                .userId(userId)
                .title(dto.getTitle())
                .content(dto.getContent())
                .image(dto.getImage())
                .longContent(dto.getLongContent())
                .originalLink(dto.getOriginalLink())
                .keywords(dto.getKeywords())
                .originalDate(dto.getOriginalDate())
                .build();
    }
    /** Entity → ArticleListDto 변환 */
    public ArticleListDto toArticleListDto(ArticleEntity article, Long userId) {
        Heart heart = heartRepository.findByMemberIdAndArticleId(userId, article.getId()).orElse(null);
        boolean isBookmarked = heart != null && heart.isBookmarked();

        CompletedDegreeDto completedDegreeDto = completedTimeRepository
                .findByMemberIdAndArticleId(userId, article.getId())
                .map(c -> new CompletedDegreeDto(c.getDegree(), c.getCompletedAt()))
                .orElse(null);

        return ArticleListDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .degree(completedDegreeDto != null ? completedDegreeDto.getDegree() : null)
                .completedTime(completedDegreeDto != null ? completedDegreeDto.getCompletedTime() : null)
                .longContent(article.getLongContent())
                .originalLink(article.getOriginalLink())
                .image(article.getImage())
                .keywords(article.getKeywords())
                .originalDate(article.getOriginalDate())
                .userId(article.getUserId())
                .likeCount(article.getLikeCount())
                .bookmarked(isBookmarked)
                .build();
    }

    /** Entity → ArticleZipListDto 변환 */
    public ArticleZipListDto toArticleZipListDto(ArticleEntity article, Long userId) {
        Heart heart = heartRepository.findByMemberIdAndArticleId(userId, article.getId()).orElse(null);
        boolean isBookmarked = heart != null && heart.isBookmarked();

        CompletedDegreeDto completedDegreeDto = completedTimeRepository
                .findByMemberIdAndArticleId(userId, article.getId())
                .map(c -> new CompletedDegreeDto(c.getDegree(), c.getCompletedAt()))
                .orElse(null);

        return ArticleZipListDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .degree(completedDegreeDto != null ? completedDegreeDto.getDegree() : null)
                .completedTime(completedDegreeDto != null ? completedDegreeDto.getCompletedTime() : null)
                .image(article.getImage())
                .keywords(article.getKeywords())
                .originalDate(article.getOriginalDate())
                .likeCount(article.getLikeCount())
                .bookmarked(isBookmarked)
                .build();
    }
    /** Entity → ArticleLongContentDto 변환 */
    public ArticleLongContentDto toArticleLongContentDto(ArticleEntity article, Long userId) {
        Heart heart = heartRepository.findByMemberIdAndArticleId(userId, article.getId()).orElse(null);
        boolean isBookmarked = heart != null && heart.isBookmarked();

        CompletedDegreeDto completedDegreeDto = completedTimeRepository
                .findByMemberIdAndArticleId(userId, article.getId())
                .map(c -> new CompletedDegreeDto(c.getDegree(), c.getCompletedAt()))
                .orElse(null);

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
}
