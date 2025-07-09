package com.draconist.goodluckynews.domain.place.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class PlaceCreateDTO {
    private String placeName;
    private String placeDetails;
    private String placeImg;
}
