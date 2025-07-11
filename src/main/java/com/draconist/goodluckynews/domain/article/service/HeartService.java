package com.draconist.goodluckynews.domain.article.service;

import com.draconist.goodluckynews.domain.article.dto.ArticleLongContentDto;
import com.draconist.goodluckynews.domain.article.dto.CompletedDegreeDto;
import com.draconist.goodluckynews.domain.article.dto.HeartDto;
import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.entity.CompletedTime;
import com.draconist.goodluckynews.domain.article.entity.Heart;
import com.draconist.goodluckynews.domain.article.repository.ArticleRepository;
import com.draconist.goodluckynews.domain.article.repository.CompletedTimeRepository;
import com.draconist.goodluckynews.domain.article.repository.HeartRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
import com.draconist.goodluckynews.global.jwt.service.CustomUserDetailsService;
import com.draconist.goodluckynews.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HeartService {
    private final HeartRepository heartRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final CompletedTimeRepository completedTimeRepository;

    //북마크 취소 삭제
    @Transactional
    public ResponseEntity<?> insert(Long articleId, Long userId) {
        // jwt확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        //Article 찾기
        ArticleEntity article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._ARTICLE_NOT_FOUND));
        //새로 생성했다면
        // 기존 Heart 객체 확인
        Heart heart = heartRepository.findByMemberAndArticle(member, article).orElse(null);
        //객체가 없으면 null
        if (heart == null) {
            heart = new Heart(member, article, true);
            heartRepository.save(heart);
            // 게시글의 좋아요 수 갱신
            article.updateLikeCount(true);
            articleRepository.save(article);

            ArticleLongContentDto responseDto = buildArticleLongContentDto(article, userId);
            return ResponseEntity.status(201).body(ApiResponse.onSuccess(responseDto));
        }

//이미 생성된 북마크가 있을때

        //북마크가 true면 이미 북마크되어 있으면
            if(heart.isBookmarked()) {
                heart.setBookmarked(false);
                heartRepository.save(heart);
                // 게시글의 좋아요 수 갱신
                article.updateLikeCount(false);
                articleRepository.save(article);
            }
            else{ //북마크 안되어 있으면 북마크
                heart.setBookmarked(true);
                heartRepository.save(heart);
                // 게시글의 좋아요 수 갱신
                article.updateLikeCount(true);
                articleRepository.save(article);
            }
        ArticleLongContentDto responseDto = buildArticleLongContentDto(article,userId);
        return ResponseEntity.status(200).body(ApiResponse.onSuccess(responseDto));
    }

    //빌드 분리
    private ArticleLongContentDto buildArticleLongContentDto(ArticleEntity article, Long userId) {
        Heart heart = heartRepository.findByMemberIdAndArticleId(userId, article.getId())
                .orElse(null);
        boolean isBookmarked = heart != null && heart.isBookmarked(); // 북마크 여부 확인
        CompletedDegreeDto completedDegreeDto = completedTimeRepository
                .findByMemberIdAndArticleId(userId, article.getId())
                .map(completedTime -> new CompletedDegreeDto(completedTime.getDegree(), completedTime.getCompletedAt()))
                .orElse(null); // 만약 없으면 null

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
