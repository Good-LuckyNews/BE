package com.draconist.goodluckynews.domain.place.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.Builder;
import lombok.*;

@Data
@Builder
@NoArgsConstructor // 🔹 기본 생성자 추가
@AllArgsConstructor
public class PlaceDTO {
    private String placeName;
    private String placeDetails;
    private String placeImg;
}


