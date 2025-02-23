package com.draconist.goodluckynews.domain.place.service;

import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.domain.place.dto.PlaceDTO;
import com.draconist.goodluckynews.domain.place.entity.Place;
import com.draconist.goodluckynews.domain.place.repository.PlaceRepository;
import com.draconist.goodluckynews.global.awss3.service.AwsS3Service;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.enums.statuscode.SuccessStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;
    private final AwsS3Service awsS3Service;

    public ResponseEntity<?> createPlace(MultipartFile image, PlaceDTO placeDTO, String email)throws IOException {
        //1. 이메일로 회원 정보 찾기
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        //2. 플레이스 이름 중복 검사
        placeRepository.findByPlaceName(placeDTO.getPlaceName()).ifPresent(community -> {
            throw new GeneralException(ErrorStatus._DUPLICATE_PLACE_NAME);
        });
        //3. 플레이스 생성
        String imageURL =null;
        if(image!=null && !image.isEmpty()){
            imageURL = awsS3Service.uploadFile(image);
        }
        Place place = Place.builder()
                .placeImg(imageURL)
                .placeName(placeDTO.getPlaceName())
                .placeDetails(placeDTO.getPlaceDetails())
                .build();
        //4. member(유저)와 플레이스 연결
        member.createPlace(place);
        //5. 플레이스 저장
        placeRepository.save(place);
        //6. status 반환
        return ResponseEntity.status(SuccessStatus._PLACE_CREATED.getHttpStatus())
                .body(ApiResponse.onSuccess(
                        SuccessStatus._PLACE_CREATED.getMessage(),
                        place // 생성된 플레이스 정보 포함
                ));
    }
}
