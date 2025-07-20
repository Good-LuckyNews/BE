package com.draconist.goodluckynews.domain.article.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
public class FirstCreatedAndTodayDto {
    private LocalDateTime firstCreatedAt;
    private LocalDateTime lastUpdatedAt;
}
