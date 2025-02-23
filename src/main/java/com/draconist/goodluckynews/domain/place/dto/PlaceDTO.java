package com.draconist.goodluckynews.domain.place.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceDTO {
    private String placeName;
    private String placeDetails;
    private String placeImg; //유일한 not null값
}
