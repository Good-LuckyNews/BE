package com.draconist.goodluckynews.domain.article.controller;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

@Slf4j
public class NewsScraper {
    public static ArticleContent fetchArticleContent(String originalLink) {
        try {
            Document doc = Jsoup.connect(originalLink).get();

            // 본문만 선택 (네이버 뉴스 기준, 다른 언론사는 추가 selector 필요)-> 클로링 안될 수도 있음
            Element contentElement = doc.selectFirst("div#newsct_article, div#articeBody, div.article_body");

            String longContent = "";
            if (contentElement != null) {
                // 불필요한 요소 제거 (댓글, 광고, 스크립트 등)
                contentElement.select("script, style, iframe, .u_cbox, #comment, .comment_area, .reply, .sns_area").remove();

                longContent = contentElement.text();
                log.info("본문 추출 성공 (길이: {})", longContent.length());
            } else {
                log.warn("본문 영역을 찾지 못함 → fallback 로직 실행");
                longContent = doc.body().text(); // fallback: 페이지 전체 텍스트
            }

            // 이미지 추출
            String image = "";
            Elements imgElements = doc.select("figure img, div#newsct_article img, div#articeBody img");
            if (!imgElements.isEmpty()) {
                image = imgElements.first().absUrl("src");
                log.info("대표 이미지 추출 성공 : {}", image);
            }

            return new ArticleContent(image, longContent.replace("\n", "<br>"));
        } catch (IOException e) {
            log.error("기사 크롤링 실패: {}", e.getMessage(), e);
            return new ArticleContent("", "");
        }
    }

    public static class ArticleContent {
        public final String image;
        public final String longContent;

        public ArticleContent(String image, String longContent) {
            this.image = image;
            this.longContent = longContent;
        }
    }
}
