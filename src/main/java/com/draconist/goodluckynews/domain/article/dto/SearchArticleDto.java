package com.draconist.goodluckynews.domain.article.dto;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Builder
public class SearchArticleDto {
    private String searchQuery;
}
