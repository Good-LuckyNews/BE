package com.draconist.goodluckynews.domain.place.controller;

import com.draconist.goodluckynews.domain.place.dto.PlaceDTO;
import com.draconist.goodluckynews.domain.place.service.PlaceService;
import com.draconist.goodluckynews.global.jwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/place")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;
    @PostMapping()
    public ResponseEntity<?> createPlace(@RequestParam(value = "image",required = false) MultipartFile image,
                @ModelAttribute PlaceDTO communityDTO,
                @AuthenticationPrincipal CustomUserDetails customUserDetails)throws IOException {
        return  placeService.createPlace(image, communityDTO, customUserDetails.getEmail());
    }
}
