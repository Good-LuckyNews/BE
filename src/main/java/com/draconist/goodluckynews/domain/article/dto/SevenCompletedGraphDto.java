package com.draconist.goodluckynews.domain.article.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SevenCompletedGraphDto {
    private Integer first;
    private Integer second;
    private Integer third;
    private Integer fourth;
    private Integer fifth;
    private Integer sixth;
    private Integer seventh;
    private LocalDateTime firstCompletedAt;  // 최초로 완료한 날짜
    private LocalDateTime lastCompletedAt;   // 마지막으로 완료한 날짜
}
