package com.draconist.goodluckynews.domain.article.service;

import com.draconist.goodluckynews.domain.article.dto.HeartDto;
import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.entity.Heart;
import com.draconist.goodluckynews.domain.article.repository.ArticleRepository;
import com.draconist.goodluckynews.domain.article.repository.HeartRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
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

    @Transactional
    public ResponseEntity<?> insert(Long articleId, Long userId) {
        // jwt확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        //Article 찾기
        ArticleEntity article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._ARTICLE_NOT_FOUND));

        // 이미 좋아요가 있다면 예외 처리
        if (heartRepository.findByMemberAndArticle(member, article).isPresent()) {
            throw new GeneralException(ErrorStatus._ALREADY_HEARTED);
        }

        // 좋아요 추가
        Heart heart = new Heart(member, article);
        heartRepository.save(heart);

        // 게시글의 좋아요 수 갱신
        article.updateLikeCount(true);
        articleRepository.save(article);
        return ResponseEntity.status(201).body(ApiResponse.onSuccess("좋아요 성공했습니다."));
    }

    @Transactional
    public ResponseEntity<?> delete(Long articleId, Long userId) {
        // jwt확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        //Article 찾기
        ArticleEntity article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._ARTICLE_NOT_FOUND));

        Heart heart = heartRepository.findByMemberAndArticle(member, article)
                .orElseThrow(() -> new GeneralException(ErrorStatus._HEART_NOT_FOUND));

        // 좋아요 취소
        heartRepository.delete(heart);

        // 게시글의 좋아요 수 갱신
        article.updateLikeCount(false);
        articleRepository.save(article);

        return ResponseEntity.status(201).body(ApiResponse.onSuccess("좋아요 삭제했습니다."));
    }
}
