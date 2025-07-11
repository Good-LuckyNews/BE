package com.draconist.goodluckynews.domain.FcmToken.repository;

import com.draconist.goodluckynews.domain.FcmToken.entity.FcmToken;
import com.draconist.goodluckynews.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByMemberAndActiveTrue(Member member);
    boolean existsByToken(String token);

    Optional<FcmToken> findByToken(String token);
}
