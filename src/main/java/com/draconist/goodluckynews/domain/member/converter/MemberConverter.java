package com.draconist.goodluckynews.domain.member.converter;

import com.draconist.goodluckynews.domain.member.dto.JoinDTO;
import com.draconist.goodluckynews.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberConverter {
    private final BCryptPasswordEncoder passwordEncoder;

    public Member toMember(JoinDTO joinDTO) {
        return Member.builder()
                .email(joinDTO.getEmail())
                .password(passwordEncoder.encode(joinDTO.getPassword()))
                .name(joinDTO.getName())
                .profileImage(joinDTO.getProfileImage())
                .amPm(joinDTO.getAmPm())
                .hours(joinDTO.getHours())
                .minutes(joinDTO.getMinutes())
                .role("ROLE_USER")
                .build();
    }
}
