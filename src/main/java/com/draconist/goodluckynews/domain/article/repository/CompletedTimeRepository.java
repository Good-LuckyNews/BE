package com.draconist.goodluckynews.domain.article.repository;

import com.draconist.goodluckynews.domain.article.entity.ArticleEntity;
import com.draconist.goodluckynews.domain.article.entity.CompletedTime;
import com.draconist.goodluckynews.domain.article.entity.Heart;
import com.draconist.goodluckynews.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompletedTimeRepository extends JpaRepository<CompletedTime, Long> {
    // 이미 완료되었는지 확인하는 메서드
    @Query("SELECT h FROM Heart h WHERE h.member = :member AND h.article = :article")
    Optional<Heart> findByMemberAndArticle(Member member, ArticleEntity article);
    // 이번 주 (오늘부터 7일간) (특정 userId에 대한 완료된 기사만)
    @Query("SELECT c FROM CompletedTime c WHERE c.completedAt >= :startOfWeek AND c.completedAt < :endOfWeek AND c.member.id = :userId")
    List<CompletedTime> findCompletedTimesThisWeek(Long userId, LocalDateTime startOfWeek, LocalDateTime endOfWeek);

    // 지난 달 (특정 userId에 대한 완료된 기사만)
    @Query("SELECT c FROM CompletedTime c WHERE c.completedAt >= :startOfMonth AND c.completedAt < :endOfMonth AND c.member.id = :userId")
    List<CompletedTime> findCompletedTimesLastMonth(Long userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth);

    // 최근 6개월 (특정 userId에 대한 완료된 기사만)
    @Query("SELECT c FROM CompletedTime c WHERE c.completedAt >= :startOfSixMonthsAgo AND c.completedAt < :endOfToday AND c.member.id = :userId")
    List<CompletedTime> findCompletedTimesLastSixMonths(Long userId, LocalDateTime startOfSixMonthsAgo, LocalDateTime endOfToday);

    // 전체 기간 (특정 userId에 대한 완료된 기사만)
    @Query("SELECT c FROM CompletedTime c WHERE c.completedAt IS NOT NULL AND c.member.id = :userId")
    List<CompletedTime> findCompletedTimesAllTime(Long userId);

    //completedTime 조회
    Optional<CompletedTime> findByMemberIdAndArticleId(Long memberId, Long articleId);
    Optional<CompletedTime> findFirstByMemberOrderByCreatedAtAsc(Member member);
}
