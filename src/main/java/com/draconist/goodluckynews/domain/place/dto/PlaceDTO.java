package com.draconist.goodluckynews.domain.place.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTO {
    private Long placeId;   // 🔹 placeId 추가
    private String placeName;
    private String placeDetails;
    private String placeImg;
}
