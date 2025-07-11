package com.draconist.goodluckynews.domain.FcmToken.controller;

import com.draconist.goodluckynews.domain.FcmToken.dto.FcmTokenResultDTO;
import com.draconist.goodluckynews.domain.FcmToken.dto.FcmTokenSaveRequestDTO;
import com.draconist.goodluckynews.domain.FcmToken.service.FcmTokenService;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/token")
    public ResponseEntity<FcmTokenResultDTO> saveFcmToken(
            @RequestBody FcmTokenSaveRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        FcmTokenResultDTO result = fcmTokenService.saveFcmToken(userDetails.getEmail(), request.getToken());
        return ResponseEntity.ok(result);
    }


}
