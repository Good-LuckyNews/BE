package com.draconist.goodluckynews.domain.member.dto;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class MemberInfoDTO {
    private String name;
    private String profileImage;
    private String amPm;
    private Integer hours;
    private Integer minutes;
    private String keywords;
}
