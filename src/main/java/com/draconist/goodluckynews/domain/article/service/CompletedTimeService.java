package com.draconist.goodluckynews.domain.article.service;

import com.draconist.goodluckynews.domain.article.dto.CompletedDegreeDto;
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
import com.draconist.goodluckynews.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;


@Service
    @RequiredArgsConstructor
    public class CompletedTimeService {
        private final CompletedTimeRepository completedTimeRepository;
        private final MemberRepository memberRepository;
        private final ArticleRepository articleRepository;

        @Transactional
        public ResponseEntity<?> WriteTime(Long articleId, Long userId, CompletedDegreeDto completedDegreeDto) {
            // jwt확인
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

            //Article 찾기
            ArticleEntity article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus._ARTICLE_NOT_FOUND));

            // 이미 완료되어 있다면 예외 처리
            if (completedTimeRepository.findByMemberAndArticle(member, article).isPresent()) {
                throw new GeneralException(ErrorStatus._ALREADY_COMPLETED);
            }

            // 현재 시간 저장
            LocalDateTime now = LocalDateTime.now();

            // 완료 시간 저장
            CompletedTime completedTime = new CompletedTime(member, article, now);
            completedTimeRepository.save(completedTime);

            // `ArticleEntity`에도 완료 시간 저장
            article.updateCompletedTime(now);
            article.updateDegree(completedDegreeDto.getDegree());
            articleRepository.save(article);
            return ResponseEntity.status(201).body(ApiResponse.onSuccess("긍정도를 기록하고 완료했습니다"));
        }

    @Transactional
    public ResponseEntity<?> UpdateTime(Long articleId, Long userId, CompletedDegreeDto completedDegreeDto) {
        // jwt확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        //Article 찾기
        ArticleEntity article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._ARTICLE_NOT_FOUND));

        // 현재 시간 저장
        LocalDateTime now = LocalDateTime.now();

        // 완료 시간 저장
        CompletedTime completedTime = new CompletedTime(member, article, now);
        completedTimeRepository.save(completedTime);

        // `ArticleEntity`에도 완료 시간 저장
        article.updateCompletedTime(now);
        article.updateDegree(completedDegreeDto.getDegree());
        articleRepository.save(article);
        return ResponseEntity.status(201).body(ApiResponse.onSuccess("긍정도를 기록하고 완료했습니다"));
    }
}
