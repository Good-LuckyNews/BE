package com.draconist.goodluckynews.domain.article.service;
import com.draconist.goodluckynews.domain.article.converter.ArticleConverter;
import com.draconist.goodluckynews.domain.article.dto.*;
import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.entity.Heart;
import com.draconist.goodluckynews.domain.article.repository.ArticleRepository;
import com.draconist.goodluckynews.domain.article.repository.CompletedTimeRepository;
import com.draconist.goodluckynews.domain.article.repository.HeartRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final HeartRepository heartRepository;
    private final ArticleConverter articleConverter;

    @Transactional
    public ResponseEntity<?> saveArticles(Long userId, List<ArticleDto> articleDtos) {
        // 1. 이메일로 회원 id 찾기
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        for (ArticleDto dto : articleDtos) {
            if (dto.getTitle() == null || dto.getTitle().isBlank()) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.onFailure(ErrorStatus._ARTICLE_TITLE_MISSING, null));
            }
            if (dto.getContent() == null || dto.getContent().isBlank()) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.onFailure(ErrorStatus._ARTICLE_CONTENT_MISSING, null));
            }
            ArticleEntity entity = articleConverter.toEntity(dto, member.getId());
            articleRepository.save(entity);
        }

        return ResponseEntity.status(201).body(ApiResponse.onSuccess("기사들이 생성되었습니다."));
    }

    //사용자 키워드에 맞는 랜덤으로 하나 가져오기
    @Transactional
    public ResponseEntity<?> getUserArticles(Long userId) {
        // 404 : 토큰에 해당하는 회원이 실제로 존재하는지 확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        // user의 keyword에 따라 랜덤으로 긁어옵니다.
        String keywords = member.getKeywords();  // "선행,봉사,활동"
        List<String> keywordList = Arrays.asList(keywords.split(","));

        // 랜덤으로 키워드 하나 선택
        String randomKeyword = keywordList.get(new Random().nextInt(keywordList.size()));
        // 해당 키워드에 맞는 랜덤 기사 가져오기
        ArticleEntity randomArticle = articleRepository.findRandomArticleByKeyword(randomKeyword);
        if (randomArticle == null) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.onFailure(ErrorStatus._ARTICLE_NOT_FOUND, null));
        }
        // 랜덤 게시글 정보 빌드 (response.result)
        ArticleListDto dto = articleConverter.toArticleListDto(randomArticle, userId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.onSuccess(dto));
    }


    //전체 기사 미리보기
    @Transactional
    public ResponseEntity<?> getAllShortArticles(Long userId, int page, int size) {
        // jwt확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        // 페이지 처리 (최신순 정렬)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")); // id 기준 내림차순 정렬
        // DB 검색 - 나의 게시글 조회(최신순)
        Page<ArticleEntity> articlePage = articleRepository.findAll(pageRequest);
        // 게시글 정보 빌드 (response.result)
        List<ArticleZipListDto> responseDtos = articlePage
                .stream()
                .map(article -> articleConverter.toArticleZipListDto(article, userId))
                .toList();

        return ResponseEntity.ok(ApiResponse.onSuccess(responseDtos));
    }

    //상세보기
    @Transactional
    public ResponseEntity<?> getArticleDetail(Long userId, Long articleId) {
        //jwt확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        //Article 찾기
        ArticleEntity article = articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._ARTICLE_NOT_FOUND));

        ArticleLongContentDto dto = articleConverter.toArticleLongContentDto(article, userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(dto));

    }

    //북마크한 기사 모아보기
    public ResponseEntity<?> getUserLikeArticles(Long userId, int page, int size) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 북마크된 기사 조회
        List<ArticleEntity> bookmarkedArticles = heartRepository.findBookmarkedArticlesByUserId(userId);

        // 페이지네이션 적용
        int start = page * size;
        int end = Math.min((page + 1) * size, bookmarkedArticles.size());
        List<ArticleEntity> pagedArticles = bookmarkedArticles.subList(start, end);

        // 게시글 정보 빌드 (response.result)
        // 응답 DTO 변환
        List<ArticleZipListDto> dtoList = bookmarkedArticles.subList(start, end)
                .stream()
                .map(a -> articleConverter.toArticleZipListDto(a, userId))
                .toList();

        return ResponseEntity.ok(ApiResponse.onSuccess(dtoList));
    }


    //검색 메소드
    public ResponseEntity<?> getSearchedArticles(Long userId, String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleEntity> articlePage = articleRepository.searchArticles(searchQuery, pageable);

        // ArticleZipListDto로 변환
        List<ArticleZipListDto> dtoList = articlePage
                .stream()
                .map(a -> articleConverter.toArticleZipListDto(a, userId))
                .toList();

        // 응답 반환
        return ResponseEntity.ok(ApiResponse.onSuccess(dtoList));
    }




}
