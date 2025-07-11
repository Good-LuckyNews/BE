package com.draconist.goodluckynews.domain.FcmToken.dto;

import com.draconist.goodluckynews.domain.FcmToken.entity.FcmToken;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmTokenResultDTO {
    private Long id;
    private String token;
    private Long userId;
    private Boolean active;

    public FcmTokenResultDTO(FcmToken fcmToken) {
        this.id = fcmToken.getId();
        this.token = fcmToken.getToken();
        this.userId = fcmToken.getMember().getId();
        this.active = fcmToken.getActive();
    }
}
