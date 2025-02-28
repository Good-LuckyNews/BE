package com.draconist.goodluckynews.domain.article.controller;

import com.draconist.goodluckynews.domain.article.dto.ArticleDto;
import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.service.ArticleService;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
import com.draconist.goodluckynews.global.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController  // @RestController로 변경
@RequiredArgsConstructor
public class ArticleController {
    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;
    private final MemberRepository memberRepository;
    private final ArticleService articleService;

    @GetMapping("/fetch-news")
    public ResponseEntity<ApiResponse<String>> fetchAndSaveNews(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        // 1. 이메일로 회원 id 찾기
        Member member = memberRepository.findMemberByEmail(customUserDetails.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        log.info("회원 ID: {}", member.getId());

        String clientId = "H0NNAl4QWz_y3_cnT92C";
        String clientSecret = "e0uIXyPkbH";

        // user의 keyword에 따라 랜덤으로 긁어옵니다.
        String keywords = member.getKeywords();  // "선행,봉사,활동"
        List<String> keywordList = Arrays.asList(keywords.split(","));

        List<ArticleDto> allArticleDtos = new ArrayList<>();

        for (String keyword : keywordList) {
            // 검색어를 UTF-8로 인코딩
            String encodedText = null;
            try {
                encodedText = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("검색어 인코딩 실패", e);
            }

            log.info("검색할 키워드: {}", keyword);

            // apiURL에 인코딩된 검색어를 포함
            String apiURL = "https://openapi.naver.com/v1/search/news?query=" + encodedText + "&display=3";

            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("X-Naver-Client-Id", clientId);
            requestHeaders.put("X-Naver-Client-Secret", clientSecret);

            String responseBody = get(apiURL, requestHeaders);
            log.info("API 응답: {}", responseBody);

            ObjectMapper om = new ObjectMapper();
            try {
                Map<String, Object> json = om.readValue(responseBody, Map.class);
                List<Map<String, Object>> newsItems = (List<Map<String, Object>>) json.get("items");

                if (newsItems == null || newsItems.isEmpty()) {
                    log.warn("뉴스 아이템이 없습니다.");
                    return ResponseEntity.badRequest().body(ApiResponse.onFailure(ErrorStatus._CRAWLFAILED, "기사 크롤링 중 오류 발생"));
                }

                // newsItems의 Map<String, Object>에서 ArticleDto로 변환
                List<ArticleDto> articleDtos = newsItems.stream()
                        .map(item -> {
                            String originalLink = (String) item.get("originallink");
                            NewsScraper.ArticleContent articleContent = NewsScraper.fetchArticleContent(originalLink);
                            return ArticleDto.builder()
                                    .title((String) item.get("title"))
                                    .originalLink((String) item.get("originallink"))
                                    .longContent(articleContent.longContent)
                                    .image(articleContent.image)
                                    .content((String) item.get("description"))
                                    .keywords(keyword)  // 각 뉴스에 키워드 추가
                                    .createdAt(LocalDateTime.now())  // 현재 시간으로 설정
                                    .build();
                        })
                        .collect(Collectors.toList());

                // articleDtos를 전체 리스트에 추가
                allArticleDtos.addAll(articleDtos);

            } catch (JsonProcessingException e) {
                log.error("JSON 처리 중 오류 발생: {}", e.getMessage());
                return ResponseEntity.badRequest().body(ApiResponse.onFailure(ErrorStatus._CRAWLFAILED, "기사 크롤링 중 오류 발생"));
            }
        }

        // 변환된 ArticleDto 리스트를 saveArticles로 전달
        articleService.saveArticles(member.getId(), allArticleDtos); // DB 저장
        log.info("새로운 기사들이 DB에 저장되었습니다.");

        return ResponseEntity.ok(ApiResponse.onSuccess("새로운 기사들이 크롤링되었습니다."));
    }

    private static String get(String apiUrl, Map<String, String> requestHeaders) {
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 오류 발생
                log.error("API 호출 실패, 응답 코드: {}", responseCode);
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            log.error("API 요청 및 응답 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            log.error("잘못된 API URL: {}", apiUrl, e);
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            log.error("연결 실패: {}", apiUrl, e);
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body) {
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            log.error("응답 읽기 실패: {}", e.getMessage());
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }

    // 유저에 등록된 기사 조회
    @GetMapping("/user")
    public ResponseEntity<?> getAllArticles(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "size", defaultValue = "3") int size) {
        // 1. 이메일로 회원 id 찾기
        Member member = memberRepository.findMemberByEmail(customUserDetails.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        log.info("회원 ID: {}, 페이지: {}, 사이즈: {}", member.getId(), page, size);

        return articleService.getUserArticles(member.getId(), page, size);
    }
}
