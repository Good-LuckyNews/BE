package com.draconist.goodluckynews.domain.article.service;
import com.draconist.goodluckynews.domain.article.dto.ArticleDto;
import com.draconist.goodluckynews.domain.article.dto.ArticleListDto;
import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.repository.ArticleRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public ResponseEntity<?> saveArticles(Long userId, List<ArticleDto> articleDtos) {
        // 1. 이메일로 회원 id 찾기
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 각 ArticleDto를 ArticleEntity로 변환하여 저장
        for (ArticleDto articleDto : articleDtos) {
            String title = articleDto.getTitle();
            String content = articleDto.getContent();

            // 400 : 제목이 비어있는 경우
            if (title == null || title.isBlank()) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.onFailure(ErrorStatus._ARTICLE_TITLE_MISSING, null));
            }

            // 400 : 내용이 비어있는 경우
            if (content == null || content.isBlank()) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.onFailure(ErrorStatus._ARTICLE_CONTENT_MISSING, null));
            }

            // Article 생성
            ArticleEntity article = ArticleEntity.builder()
                    .userId(member.getId())
                    .title(title)
                    .content(content)
                    .image(articleDto.getImage())
                    .longContent(articleDto.getLongContent())
                    .originalLink(articleDto.getOriginalLink())
                    .keywords(articleDto.getKeywords()) // ArticleDto의 keywords를 사용
                    .build();

            // DB에 기사 저장
            articleRepository.save(article);
        }

        // 201 : 기사 생성 성공
        return ResponseEntity.status(201).body(ApiResponse.onSuccess("기사들이 생성되었습니다."));
    }

    //조회
    @Transactional
    public ResponseEntity<?> getUserArticles(Long userId, int page, int size) {
        // 404 : 토큰에 해당하는 회원이 실제로 존재하는지 확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 페이지 처리
        PageRequest pageRequest = PageRequest.of(page, size); // page, size 설정

        // DB 검색 - 나의 게시글 조회(최신순)
        Page<ArticleEntity> articlePage = articleRepository.findAllByUserId(member.getId(), pageRequest);

        // 게시글 정보 빌드 (response.result)
        List<ArticleListDto> responseDtos = new ArrayList<>();
        for (ArticleEntity article : articlePage.getContent()) {
            ArticleListDto responseDto = buildArticleListResponse(article, userId);
            responseDtos.add(responseDto);
        }

        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDtos));
    }



    //빌드 편의 메소드 converter
    private ArticleListDto buildArticleListResponse(ArticleEntity article, Long userId) {
        // ArticleListDto 빌드
        return ArticleListDto.builder()
                .id(article.getId())  // ArticleEntity의 id를 ArticleListDto로 매핑
                .title(article.getTitle())  // Title 매핑
                .content(article.getContent())  // Content 매핑
                .degree(article.getDegree())  // Degree 매핑
                .longContent(article.getLongContent())
                .originalLink(article.getOriginalLink())
                .completedTime(article.getCompletedTime())  // CompletedTime 매핑
                .image(article.getImage())  // 이미지 URL을 하나로 매핑
                .keywords(article.getKeywords())  // 키워드 매핑
                .userId(article.getUserId())  // 작성자 ID
                .build();
    }



}
