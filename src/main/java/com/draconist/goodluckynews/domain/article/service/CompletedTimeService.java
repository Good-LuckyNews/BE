package com.draconist.goodluckynews.domain.article.service;

import com.draconist.goodluckynews.domain.article.converter.CompletedTimeConverter;
import com.draconist.goodluckynews.domain.article.dto.ArticleLongContentDto;
import com.draconist.goodluckynews.domain.article.dto.CompletedDegreeDto;
import com.draconist.goodluckynews.domain.article.dto.FirstCreatedAndTodayDto;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;


@Service
    @RequiredArgsConstructor
    public class CompletedTimeService {
        private final CompletedTimeRepository completedTimeRepository;
        private final MemberRepository memberRepository;
        private final ArticleRepository articleRepository;
        private final HeartRepository heartRepository;
    private final CompletedTimeConverter completedTimeConverter;

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
        Integer[] completedArticlesPerDay = new Integer[7]; // 0 = 월, 1 = 화, ..., 6 = 일
        Arrays.fill(completedArticlesPerDay, 0);
        for (CompletedTime completedTime : completedTimes) {
            LocalDateTime completedAt = completedTime.getCompletedAt();
            if (completedAt.isAfter(startOfWeek) && completedAt.isBefore(endOfWeek)) {
                int dayOfWeek = completedAt.getDayOfWeek().getValue() - 1; // 월요일: 0, 일요일: 6
                if (completedArticlesPerDay[dayOfWeek] == null) {
                    completedArticlesPerDay[dayOfWeek] = completedTime.getDegree();
                } else {
                    completedArticlesPerDay[dayOfWeek] += completedTime.getDegree();
                }
            }

        }
        LocalDateTime firstCompletedAt = getFirstCompletedAt(userId, startOfWeek, endOfWeek);
        LocalDateTime lastCompletedAt = getLastCompletedAt(userId, startOfWeek, endOfWeek);

        // SevenCompletedGraphDto에 완료된 기사 개수 세팅
        SevenCompletedGraphDto responseDto = completedTimeConverter
                .toSevenCompletedGraphDto(completedArticlesPerDay, firstCompletedAt, lastCompletedAt);
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
        Integer[] completedArticlesPerWeek = new Integer[5];// 5개의 주로 나누기 (한 달을 4~5주로 나눠야 하므로 5개로 준비)
        Arrays.fill(completedArticlesPerWeek, 0);
        // 시작일과 끝일을 기준으로 주차 계산
        for (CompletedTime completedTime : completedTimes) {
            LocalDateTime completedDate = completedTime.getCompletedAt();

            // 첫날부터 마지막 날까지 주 단위로 나누기
            int weekOfMonth = getWeekOfMonth(completedDate, firstDayOfLastMonth);
            if (completedArticlesPerWeek[weekOfMonth] == null) {
                completedArticlesPerWeek[weekOfMonth] = completedTime.getDegree();
            } else {
                completedArticlesPerWeek[weekOfMonth] += completedTime.getDegree();
            }

        }

        // SevenCompletedGraphDto에 완료된 기사 개수 세팅
        LocalDateTime firstCompletedAt = getFirstCompletedAt(userId, firstDayOfLastMonth, lastDayOfLastMonth);
        LocalDateTime lastCompletedAt = getLastCompletedAt(userId, firstDayOfLastMonth, lastDayOfLastMonth);

        SevenCompletedGraphDto responseDto = completedTimeConverter.toSevenCompletedGraphDto(
                completedArticlesPerWeek, firstCompletedAt, lastCompletedAt);

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
        Integer[] completedArticlesPerMonth = new Integer[6];// 이번 6개월 동안의 완료된 기사 개수를 담을 배열
        Arrays.fill(completedArticlesPerMonth, 0);
        // 6개월 동안의 기사를 각 달별로 세기
        for (CompletedTime completedTime : completedTimes) {
            LocalDateTime completedAt = completedTime.getCompletedAt();

            int yearDiff = now.getYear() - completedAt.getYear();
            int monthDiff = yearDiff * 12 + now.getMonthValue() - completedAt.getMonthValue();

            if (monthDiff >= 0 && monthDiff < completedArticlesPerMonth.length) {
                if (completedArticlesPerMonth[monthDiff] == null) {
                    completedArticlesPerMonth[monthDiff] = completedTime.getDegree();
                } else {
                    completedArticlesPerMonth[monthDiff] += completedTime.getDegree();
                }
            }
        }


        // SevenCompletedGraphDto에 완료된 기사 개수 세팅
        LocalDateTime firstCompletedAt = getFirstCompletedAt(userId, startOfSixMonthsAgo, endOfToday);
        LocalDateTime lastCompletedAt  = getLastCompletedAt(userId, startOfSixMonthsAgo, endOfToday);

        SevenCompletedGraphDto responseDto = completedTimeConverter.toSevenCompletedGraphDto(
                completedArticlesPerMonth, firstCompletedAt, lastCompletedAt
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(responseDto));
    }


//전체기간 기준 6등분at
@Transactional
public ResponseEntity<?> getCompletedTimesAll(Long userId) {
    Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    // 전체 기간 동안 완료된 기사 조회
    List<CompletedTime> completedTimes = completedTimeRepository.findCompletedTimesAllTime(userId);

    // 1. 기본 배열 초기화 (6등분)
    Integer[] completedArticlesPerPeriod = new Integer[6];
    for (int i = 0; i < 6; i++) {
        completedArticlesPerPeriod[i] = 0;
    }

    // 2. 완료된 기사가 없으면 그대로 반환
    if (completedTimes.isEmpty()) {
        // first~sixth는 이미 0, seventh는 null로 (converter에서 세팅)
        SevenCompletedGraphDto emptyResponseDto =
                completedTimeConverter.toSevenCompletedGraphDto(completedArticlesPerPeriod, null, null);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.onSuccess(emptyResponseDto));
    }

    // 3. 최신순 정렬
    completedTimes.sort((t1, t2) -> t2.getCompletedAt().compareTo(t1.getCompletedAt()));

    // 4. 날짜 범위
    LocalDateTime minDate = completedTimes.get(completedTimes.size() - 1).getCompletedAt();
    LocalDateTime maxDate = completedTimes.get(0).getCompletedAt();

    // 5. 6등분 기간 계산
    long totalDuration = ChronoUnit.DAYS.between(minDate, maxDate);
    long periodDuration = totalDuration / 6;
    if (periodDuration == 0) {
        periodDuration = 1;
    }

    // 7. 카운팅
    for (CompletedTime completedTime : completedTimes) {
        long daysBetween = ChronoUnit.DAYS.between(minDate, completedTime.getCompletedAt());
        int periodIndex = (int) (daysBetween / periodDuration);
        if (periodIndex >= 6) { // 최대 인덱스 5
            periodIndex = 5;
        }
        completedArticlesPerPeriod[periodIndex] += completedTime.getDegree();
    }

    // 최초 및 최종 완료 날짜
    CompletedTime first = completedTimeRepository.findFirstByMemberOrderByCreatedAtAsc(member)
            .orElseThrow(() -> new GeneralException(ErrorStatus._COMPLETED_NOTFOUND));

    CompletedTime last = completedTimeRepository.findFirstByMemberOrderByCreatedAtDesc(member)
            .orElseThrow(() -> new GeneralException(ErrorStatus._COMPLETED_NOTFOUND));

    LocalDateTime firstCreatedAt = first.getCreatedAt();
    LocalDateTime lastCreatedAt = last.getCreatedAt();

    SevenCompletedGraphDto responseDto =
            completedTimeConverter.toSevenCompletedGraphDto(completedArticlesPerPeriod, firstCreatedAt, lastCreatedAt);

    return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.onSuccess(responseDto));
}

    private ArticleLongContentDto buildArticleLongContentDto(ArticleEntity article, Long userId) {
        Heart heart = heartRepository.findByMemberIdAndArticleId(userId, article.getId())
                .orElse(null);
        CompletedDegreeDto completedDegreeDto = completedTimeRepository
                .findByMemberIdAndArticleId(userId, article.getId())
                .map(completedTime -> new CompletedDegreeDto(completedTime.getDegree(), completedTime.getCompletedAt()))
                .orElse(null);

        // 여기서 converter 사용
        return completedTimeConverter.toArticleLongContentDto(article, heart, completedDegreeDto);
    }

    public FirstCreatedAndTodayDto getFirstCreatedAndToday(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        //가장과거
        CompletedTime first =
                completedTimeRepository.findFirstByMemberOrderByCreatedAtAsc(member)
                        .orElseThrow(() -> new GeneralException(ErrorStatus._COMPLETED_NOTFOUND));

        // 가장 마지막(가장 최근)
        CompletedTime last = completedTimeRepository.findFirstByMemberOrderByCreatedAtDesc(member)
                .orElseThrow(() -> new GeneralException(ErrorStatus._COMPLETED_NOTFOUND));

        LocalDateTime firstCreatedAt = first.getCreatedAt();
        LocalDateTime lastCreatedAt = last.getCreatedAt();

        return FirstCreatedAndTodayDto.builder()
                .firstCreatedAt(firstCreatedAt)
                .lastUpdatedAt(lastCreatedAt) // now today는 마지막 완료 날짜
                .build();
    }
    private LocalDateTime getFirstCompletedAt(Long memberId, LocalDateTime start, LocalDateTime end) {
        return completedTimeRepository
                .findFirstByMemberIdAndCreatedAtBetweenOrderByCreatedAtAsc(memberId, start, end)
                .map(CompletedTime::getCreatedAt)
                .orElse(null);
    }

    private LocalDateTime getLastCompletedAt(Long memberId, LocalDateTime start, LocalDateTime end) {
        return completedTimeRepository
                .findFirstByMemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(memberId, start, end)
                .map(CompletedTime::getCreatedAt)
                .orElse(null);
    }
}
