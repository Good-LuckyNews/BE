package com.draconist.goodluckynews.domain.article.service;
import com.draconist.goodluckynews.domain.article.dto.*;
import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final HeartRepository heartRepository;
    private final CompletedTimeRepository completedTimeRepository;

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
                    .originalDate(articleDto.getOriginalDate())
                    .build();

            // DB에 기사 저장
            articleRepository.save(article);
        }

        // 201 : 기사 생성 성공
        return ResponseEntity.status(201).body(ApiResponse.onSuccess("기사들이 생성되었습니다."));
    }

    //사용자가 만든 기사 확인, 랜덤으로 하나 가져오기
    @Transactional
    public ResponseEntity<?> getUserArticles(Long userId) {
        // 404 : 토큰에 해당하는 회원이 실제로 존재하는지 확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 랜덤으로 게시글 하나 가져오기
        ArticleEntity randomArticle = articleRepository.findRandomArticleByUserId(userId);
        if (randomArticle == null) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.onFailure(ErrorStatus._ARTICLE_NOT_FOUND, null));
        }
        // 랜덤 게시글 정보 빌드 (response.result)
        ArticleListDto randomArticleDto = buildArticleListResponse(randomArticle, userId);

        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(randomArticleDto));
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
        List<ArticleZipListDto> responseDtos = new ArrayList<>();
        for (ArticleEntity article : articlePage.getContent()) {
            ArticleZipListDto responseDto = buildArticleZipListResponse(userId,article);
            responseDtos.add(responseDto);
        }
        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDtos));
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
        ArticleLongContentDto responseDto = buildArticleLongContentDto(article,userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDto));

    }

    //북마크한 기사 모아보기
    public ResponseEntity<?> getUserLikeArticles(Long userId, int page, int size) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 페이지 처리 (최신순 정렬)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")); // createdAt 기준 내림차순 정렬

        // DB에서 북마크한 기사 조회
        Page<ArticleEntity> likedArticlesPage = heartRepository.findAllLikedArticlesByUserId(userId, pageRequest);

        // 게시글 정보 빌드 (response.result)
        List<ArticleZipListDto> responseDtos = new ArrayList<>();
        for (ArticleEntity article : likedArticlesPage.getContent()) {
            ArticleZipListDto responseDto = buildArticleZipListResponse(userId,article);
            responseDtos.add(responseDto);
        }

        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDtos));
    }


    //검색 메소드
    public ResponseEntity<?> getSearchedArticles(Long userId, String searchQuery, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ArticleEntity> articlePage = articleRepository.searchArticles(searchQuery, pageable);

        // ArticleZipListDto로 변환
        List<ArticleZipListDto> responseDtos = articlePage.stream()
                .map(article -> ArticleZipListDto.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .image(article.getImage())
                        .keywords(article.getKeywords())
                        .createdAt(article.getCreatedAt())
                        .originalDate(article.getOriginalDate())
                        .likeCount(article.getLikeCount())
                        .build())
                .collect(Collectors.toList());

        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDtos));
    }

    //빌드 편의 메소드 converter
    private ArticleListDto buildArticleListResponse(ArticleEntity article, Long userId) {
        boolean isBookmarked = heartRepository.existsByMemberIdAndArticleId(userId, article.getId());
        CompletedDegreeDto completedDegreeDto = completedTimeRepository
                .findByMemberIdAndArticleId(userId, article.getId())
                .map(completedTime -> new CompletedDegreeDto(completedTime.getDegree(), completedTime.getCompletedAt()))
                .orElse(null); // 만약 없으면 null

        // ArticleListDto 빌드
        return ArticleListDto.builder()
                .id(article.getId())  // ArticleEntity의 id를 ArticleListDto로 매핑
                .title(article.getTitle())  // Title 매핑
                .content(article.getContent())  // Content 매핑
                .degree(completedDegreeDto != null ? completedDegreeDto.getDegree() : null)
                .completedTime(completedDegreeDto != null ? completedDegreeDto.getCompletedTime() : null)
                .longContent(article.getLongContent())
                .originalLink(article.getOriginalLink())
                .image(article.getImage())  // 이미지 URL을 하나로 매핑
                .keywords(article.getKeywords())  // 키워드 매핑
                .originalDate(article.getOriginalDate())
                .userId(article.getUserId())  // 작성자 ID
                .likeCount(article.getLikeCount())
                .bookmarked(isBookmarked)
                .build();
    }
    private ArticleZipListDto buildArticleZipListResponse(Long userId,ArticleEntity article) {
        boolean isBookmarked = heartRepository.existsByMemberIdAndArticleId(userId, article.getId());
        CompletedDegreeDto completedDegreeDto = completedTimeRepository
                .findByMemberIdAndArticleId(userId, article.getId())
                .map(completedTime -> new CompletedDegreeDto(completedTime.getDegree(), completedTime.getCompletedAt()))
                .orElse(null); // 만약 없으면 null

        // ArticleListDto 빌드
        return ArticleZipListDto.builder()
                .id(article.getId())  // ArticleEntity의 id를 ArticleListDto로 매핑
                .title(article.getTitle())  // Title 매핑
                .content(article.getContent())  // Content 매핑
                .degree(completedDegreeDto != null ? completedDegreeDto.getDegree() : null)
                .completedTime(completedDegreeDto != null ? completedDegreeDto.getCompletedTime() : null)
                .image(article.getImage())  // 이미지 URL을 하나로 매핑
                .keywords(article.getKeywords())  // 키워드 매핑
                .originalDate(article.getOriginalDate())
                .likeCount(article.getLikeCount())
                .bookmarked(isBookmarked)
                .build();
    }

    private ArticleLongContentDto buildArticleLongContentDto(ArticleEntity article, Long userId) {
        boolean isBookmarked = heartRepository.existsByMemberIdAndArticleId(userId, article.getId());
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
