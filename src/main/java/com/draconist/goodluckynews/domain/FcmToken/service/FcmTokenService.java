package com.draconist.goodluckynews.domain.FcmToken.service;

import com.draconist.goodluckynews.domain.FcmToken.dto.FcmTokenResultDTO;
import com.draconist.goodluckynews.domain.FcmToken.entity.FcmToken;
import com.draconist.goodluckynews.domain.FcmToken.repository.FcmTokenRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {
    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FcmTokenResultDTO saveFcmToken(String email, String token) {
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        // 이미 존재하는 토큰이면 해당 토큰 반환
        FcmToken fcmToken = fcmTokenRepository.findByToken(token)
                .orElseGet(() -> {
                    FcmToken newToken = FcmToken.builder()
                            .member(member)
                            .token(token)
                            .active(true)
                            .build();
                    return fcmTokenRepository.save(newToken);
                });

        return new FcmTokenResultDTO(fcmToken);
    }




}

