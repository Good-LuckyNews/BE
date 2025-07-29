package com.draconist.goodluckynews.domain.member.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WriterInfoDto {
    private Long userId;
    private String name;           // 작성자 닉네임
    private String profileImage;   // 프로필 이미지
}