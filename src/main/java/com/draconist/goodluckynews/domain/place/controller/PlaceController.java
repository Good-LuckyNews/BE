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
    } //플레이스 생성

    @DeleteMapping("/{placeId}")
    public ResponseEntity<?> deletePlace(
            @PathVariable Long placeId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return placeService.deletePlace(placeId, customUserDetails.getEmail());
    } //플레이스 삭제


    @GetMapping
    public ResponseEntity<?> getAllPlaces(
            @RequestParam(defaultValue = "0") int page,  // 기본값 0
            @RequestParam(defaultValue = "10") int size // 기본값 10
    ) {
        return placeService.findAllWithPagination(page, size);
    }//플레이스 전체 조회


    @GetMapping("/{placeId}")
    public ResponseEntity<?> getPlaceById(@PathVariable Long placeId) {
        return placeService.getPlaceById(placeId);
    }//특정 플레이스 상세 조회

    @PostMapping("/{placeId}")
    public ResponseEntity<?> updatePlace(
            @PathVariable Long placeId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "placeName") String placeName,
            @RequestParam(value = "placeDetails") String placeDetails,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) throws IOException {

        PlaceDTO placeDTO = PlaceDTO.builder()
                .placeName(placeName)
                .placeDetails(placeDetails)
                .build();

        return placeService.updatePlace(placeId, image, placeDTO, customUserDetails.getEmail());
    }

//특정 플레이스 수정



}
