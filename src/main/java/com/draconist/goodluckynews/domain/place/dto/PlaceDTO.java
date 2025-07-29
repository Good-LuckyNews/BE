package com.draconist.goodluckynews.domain.place.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTO {
    private Long placeId;   // placeId 추가
    private String placeName;
    private String placeDetails;
    private String placeImg;
    private int likeCount; // 좋아요 수 추가
    private boolean isLiked; // 좋아요 여부
    private boolean isBookmark; // 북마크 여부 추가 (이전 isLiked → isBookmark)

}
