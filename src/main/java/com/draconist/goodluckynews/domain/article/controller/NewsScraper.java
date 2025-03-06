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

            // figure 태그 내의 이미지 가져오기: 가장 큰 이미지를 찾기
            String image = "";
            Elements imgElements = doc.select("figure img"); // figure 태그 내의 img 태그 선택
            if (!imgElements.isEmpty()) {
                image = imgElements.stream()
                        .map(img -> img.absUrl("src")) // absUrl을 사용하여 상대경로를 절대경로로 변환
                        .max((src1, src2) -> Integer.compare(src1.length(), src2.length()))  // 가장 큰 이미지 선택
                        .orElse("");
                log.info("가장 큰 이미지 URL: {}", image);
            }

            // 본문 내용 가져오기: 모든 태그 중 텍스트가 가장 긴 것을 추출
            String longContent = "";
            Elements allElements = doc.select("*");  // 모든 태그를 선택
            for (Element element : allElements) {
                Elements links = element.select("a"); // 현재 요소 내 a 태그 선택
                for (Element link : links) {
                    link.remove(); // a 태그 자체를 제거하여 내부 텍스트를 포함하지 않도록 함
                }

                String text = element.text(); // a 태그 제거 후 텍스트 가져오기
                if (text.length() > longContent.length()) {
                    longContent = text;
                }
            }

            if (!longContent.isEmpty()) {
                log.info("가장 긴 본문 내용 추출 성공");
            } else {
                log.warn("본문 내용이 포함된 요소를 찾을 수 없습니다.");
            }

            // 줄바꿈(\n)을 <br> 태그로 변환
            longContent = longContent.replace("\n", "<br>");

            return new ArticleContent(image, longContent);
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
