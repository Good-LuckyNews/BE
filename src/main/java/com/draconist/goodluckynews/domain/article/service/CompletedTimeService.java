package com.draconist.goodluckynews.domain.article.service;

import com.draconist.goodluckynews.domain.article.dto.ArticleLongContentDto;
import com.draconist.goodluckynews.domain.article.dto.CompletedDegreeDto;
import com.draconist.goodluckynews.domain.article.dto.SevenCompletedGraphDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
    @RequiredArgsConstructor
    public class CompletedTimeService {
        private final CompletedTimeRepository completedTimeRepository;
        private final MemberRepository memberRepository;
        private final ArticleRepository articleRepository;
        private final HeartRepository heartRepository;

        @Transactional
        public ResponseEntity<?> WriteTime(Long articleId, Long userId, CompletedDegreeDto completedDegreeDto) {
            // jwt확인
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

            // Article 찾기
            ArticleEntity article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus._ARTICLE_NOT_FOUND));

            // 현재 시간 저장
            LocalDateTime now = LocalDateTime.now();

            // 이미 완료 시간이 존재하는지 확인
            CompletedTime existingCompletedTime = completedTimeRepository
                    .findByMemberIdAndArticleId(userId, articleId)
                    .orElse(null);

            if (existingCompletedTime != null) {
                // 이미 완료 시간이 존재하면 수정
                existingCompletedTime.setDegree(completedDegreeDto.getDegree());  // 긍정도를 업데이트
                existingCompletedTime.setCompletedAt(now);  // 완료 시간을 업데이트
                completedTimeRepository.save(existingCompletedTime);  // 업데이트된 객체 저장
                ArticleLongContentDto responseDto = buildArticleLongContentDto(article,userId);
                return ResponseEntity.status(200).body(ApiResponse.onSuccess(responseDto));
            } else {
                // 완료 시간이 없으면 새로 생성
                CompletedTime completedTime = new CompletedTime(member, article, now, completedDegreeDto.getDegree());
                completedTimeRepository.save(completedTime);
                ArticleLongContentDto responseDto = buildArticleLongContentDto(article,userId);
                return ResponseEntity.status(201).body(ApiResponse.onSuccess(responseDto));
            }
        }
    //그래프 값



    //이번주 그래프
    @Transactional
    public ResponseEntity<?> getCompletedTimesThisWeek(Long userId) {
        // jwt 확인 (간략화)
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 현재 시간 및 주 시작과 끝 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        // 이번 주의 완료된 기사 조회
        List<CompletedTime> completedTimes = completedTimeRepository.findCompletedTimesThisWeek(userId,startOfWeek, endOfWeek);

        // 각 날마다 완료된 기사의 개수를 셈
        int[] completedArticlesPerDay = new int[7]; // 0 = 월, 1 = 화, ..., 6 = 일

        for (CompletedTime completedTime : completedTimes) {
            LocalDateTime completedAt = completedTime.getCompletedAt();
            if (completedAt.isAfter(startOfWeek) && completedAt.isBefore(endOfWeek)) {
                int dayOfWeek = completedAt.getDayOfWeek().getValue() - 1; // 월요일: 0, 일요일: 6
                completedArticlesPerDay[dayOfWeek] += completedTime.getDegree();
            }
        }

        // SevenCompletedGraphDto에 완료된 기사 개수 세팅
        SevenCompletedGraphDto responseDto = SevenCompletedGraphDto.builder()
                .first(completedArticlesPerDay[0])
                .second(completedArticlesPerDay[1])
                .third(completedArticlesPerDay[2])
                .fourth(completedArticlesPerDay[3])
                .fifth(completedArticlesPerDay[4])
                .sixth(completedArticlesPerDay[5])
                .seventh(completedArticlesPerDay[6])
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDto));

    }
//저번달 그래프
    @Transactional
    public ResponseEntity<?> getCompletedTimesLastMonth(Long userId) {
        // jwt 확인 (간략화)
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 현재 시간에서 한 달 전으로 이동
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime lastDayOfLastMonth = firstDayOfLastMonth.plusMonths(1).minusDays(1).with(LocalTime.MAX); // 마지막 날의 마지막 시간

        // 저번 달의 완료된 기사 조회
        List<CompletedTime> completedTimes = completedTimeRepository.findCompletedTimesLastMonth(userId,firstDayOfLastMonth, lastDayOfLastMonth);

        // 각 주에 완료된 기사 개수를 셈
        int[] completedArticlesPerWeek = new int[5]; // 5개의 주로 나누기 (한 달을 4~5주로 나눠야 하므로 5개로 준비)

        // 시작일과 끝일을 기준으로 주차 계산
        for (CompletedTime completedTime : completedTimes) {
            LocalDateTime completedDate = completedTime.getCompletedAt();

            // 첫날부터 마지막 날까지 주 단위로 나누기
            int weekOfMonth = getWeekOfMonth(completedDate, firstDayOfLastMonth);
            if (weekOfMonth >= 0 && weekOfMonth < completedArticlesPerWeek.length) {
                completedArticlesPerWeek[weekOfMonth] +=completedTime.getDegree();;
            }
        }

        // SevenCompletedGraphDto에 완료된 기사 개수 세팅
        SevenCompletedGraphDto responseDto = SevenCompletedGraphDto.builder()
                .first(completedArticlesPerWeek[0])
                .second(completedArticlesPerWeek[1])
                .third(completedArticlesPerWeek[2])
                .fourth(completedArticlesPerWeek[3])
                .fifth(completedArticlesPerWeek[4]) // 주가 5개 초과일 수 없음
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDto));
    }

    // 주차 계산 함수: 해당 날짜가 몇 번째 주에 속하는지 계산
    private int getWeekOfMonth(LocalDateTime completedDate, LocalDateTime firstDayOfLastMonth) {
        // 첫 날부터 얼마나 떨어져 있는지 구해서 주차를 계산
        long daysBetween = ChronoUnit.DAYS.between(firstDayOfLastMonth, completedDate);
        return (int) (daysBetween / 7); // 7일 단위로 나누기
    }

    @Transactional
    public ResponseEntity<?> getCompletedTimesLastSixMonth(Long userId) {
        // jwt 확인 (간략화)
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 현재 날짜 부터 6개월 전까지 날짜 계산 (오늘 포함)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfSixMonthsAgo = now.minusMonths(6).withHour(0).withMinute(0).withSecond(0).withNano(0);  // 6개월 전부터
        LocalDateTime endOfToday = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999); // 오늘의 끝까지

        // 이번 6개월 동안의 완료된 기사 조회
        List<CompletedTime> completedTimes = completedTimeRepository.findCompletedTimesLastSixMonths(userId,startOfSixMonthsAgo, endOfToday);

        // 6개월 동안 각 달마다 완료된 기사 수를 셈
        int[] completedArticlesPerMonth = new int[6]; // 이번 6개월 동안의 완료된 기사 개수를 담을 배열

        // 6개월 동안의 기사를 각 달별로 세기
        for (CompletedTime completedTime : completedTimes) {
            LocalDateTime completedAt = completedTime.getCompletedAt();

            // 6개월 전부터 오늘까지의 기간 내에서 각 달에 해당하는 기사 세기
            int monthDiff = now.getMonthValue() - completedAt.getMonthValue();
            if (monthDiff < 0) {
                monthDiff += 12;  // 월 차이가 음수일 경우, 12개월을 더해서 양수로 계산
            }

            if (monthDiff < 6) {
                completedArticlesPerMonth[monthDiff] += completedTime.getDegree();
            }
        }

        // SevenCompletedGraphDto에 완료된 기사 개수 세팅
        SevenCompletedGraphDto responseDto = SevenCompletedGraphDto.builder()
                .first(completedArticlesPerMonth[0])   // 현재 월
                .second(completedArticlesPerMonth[1])
                .third(completedArticlesPerMonth[2])
                .fourth(completedArticlesPerMonth[3])
                .fifth(completedArticlesPerMonth[4])
                .sixth(completedArticlesPerMonth[5])   // 6개월 전 월
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDto));
    }


//전체기간 기준 6등분
    @Transactional
    public ResponseEntity<?> getCompletedTimesAll(Long userId) {
        // jwt 확인 (간략화)
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 전체 기간 동안 완료된 기사 조회
        List<CompletedTime> completedTimes = completedTimeRepository.findCompletedTimesAllTime(userId);

        // completedTimes가 비어있으면 바로 0값으로 반환
        if (completedTimes.isEmpty()) {
            SevenCompletedGraphDto emptyResponseDto = SevenCompletedGraphDto.builder()
                    .first(0)
                    .second(0)
                    .third(0)
                    .fourth(0)
                    .fifth(0)
                    .sixth(0)
                    .seventh(0)
                    .build();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.onSuccess(emptyResponseDto));
        }

        // 전체 기간동안 완료한 기사를 최신 순으로 정렬
        completedTimes.sort((t1, t2) -> t2.getCompletedAt().compareTo(t1.getCompletedAt())); // 최신 기사가 first에 오도록 정렬

        // 가장 오래된 날짜와 최신 날짜 구하기
        LocalDateTime minDate = completedTimes.get(completedTimes.size() - 1).getCompletedAt(); // 가장 오래된 날짜
        LocalDateTime maxDate = completedTimes.get(0).getCompletedAt(); // 가장 최신 날짜

        // 기간 범위를 6등분하기 위한 interval 계산
        long totalDuration = ChronoUnit.DAYS.between(minDate, maxDate);

        // 기간이 6일 미만일 경우 periodDuration이 0이 될 수 있기 때문에 최소 1로 설정
        long periodDuration = totalDuration / 6;
        if (periodDuration == 0) {
            periodDuration = 1; // 6일 미만일 경우 1일 기준으로 간주
        }

        // 6등분된 각 구간에 대해 완료된 기사 수를 셈
        int[] completedArticlesPerPeriod = new int[6];

        for (CompletedTime completedTime : completedTimes) {
            long daysBetween = ChronoUnit.DAYS.between(minDate, completedTime.getCompletedAt());

            // 해당 기사가 속하는 기간을 계산
            int periodIndex = (int) (daysBetween / periodDuration);

            // 만약 periodIndex가 6보다 크거나 같으면 마지막 기간에 포함시킴
            if (periodIndex >= 6) {
                periodIndex = 5;
            }

            completedArticlesPerPeriod[periodIndex] += completedTime.getDegree();
        }

        // SevenCompletedGraphDto에 완료된 기사 개수 세팅 (최신이 first에 오도록)
        SevenCompletedGraphDto responseDto = SevenCompletedGraphDto.builder()
                .first(completedArticlesPerPeriod[0])   // 가장 최신 구간
                .second(completedArticlesPerPeriod[1])
                .third(completedArticlesPerPeriod[2])
                .fourth(completedArticlesPerPeriod[3])
                .fifth(completedArticlesPerPeriod[4])
                .sixth(completedArticlesPerPeriod[5])
                .seventh(0)  // 만약 구간이 6개보다 적으면 나머지는 0
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDto));
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
