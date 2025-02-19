package com.draconist.goodluckynews.domain.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinDTO {
    private String email;
    private String password;
    private String name;
    private String profileImage;
    private String amPm;
    private int hours;
    private int minutes;
}

