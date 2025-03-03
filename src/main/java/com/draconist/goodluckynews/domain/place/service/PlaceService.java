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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


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
    }//플레이스 생성


    public ResponseEntity<?> deletePlace(Long placeId, String email) {
        // 1. 이메일로 회원 정보 찾기
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. placeId로 Place 조회
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLACE_NOT_FOUND));

        // 3. 현재 로그인한 사용자가 Place의 소유자인지 확인
        if (!place.getUserId().equals(member.getId())) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED_ACCESS);
        }

        // 4. S3에 저장된 이미지 삭제 (있을 경우)
        if (place.getPlaceImg() != null) {
            awsS3Service.deleteFile(place.getPlaceImg());
        }

        // 5. Place 삭제
        placeRepository.delete(place);

        // 6. 성공 응답 반환
        return ResponseEntity.status(SuccessStatus._PLACE_DELETED.getHttpStatus())
                .body(ApiResponse.onSuccess(
                        SuccessStatus._PLACE_DELETED.getMessage(),
                        "Place 삭제 완료"
                ));
    }//플레이스 삭제

    public ResponseEntity<?> findAllWithPagination(int page, int size) {
        // 1. 페이지 번호가 음수 또는 0 이하인 경우 예외 발생
        if (page < 0 || size <= 0) {
            throw new GeneralException(ErrorStatus._PAGE_INVALID_REQUEST);
        }

        // 2. 페이지네이션 적용하여 데이터 조회
        Pageable pageable = PageRequest.of(page, size);
        Page<Place> placePage = placeRepository.findAll(pageable);

        // 3. 조회된 데이터가 없는 경우 예외 처리
        if (placePage.isEmpty()) {
            throw new GeneralException(ErrorStatus._PAGE_EMPTY_RESULT);
        }

        // 4. 조회된 데이터를 DTO로 변환
        Page<PlaceDTO> placeDTOPage = placePage.map(place ->
                PlaceDTO.builder()
                        .placeName(place.getPlaceName())
                        .placeDetails(place.getPlaceDetails())
                        .placeImg(place.getPlaceImg())
                        .build()
        );

        // 5. 성공 응답 반환
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus._PLACE_PAGINATION_SUCCESS.getMessage(),
                placeDTOPage
        ));
    }//플레이스 전체 조회

    public ResponseEntity<?> getPlaceById(Long placeId) {
        // 1. placeId로 Place 조회 (없으면 예외 발생)
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLACE_NOT_FOUND));

        // 2. DTO로 변환
        PlaceDTO placeDTO = PlaceDTO.builder()
                .placeName(place.getPlaceName())
                .placeDetails(place.getPlaceDetails())
                .placeImg(place.getPlaceImg())
                .build();

        // 3. 성공 응답 반환
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus._PLACE_DETAIL_SUCCESS.getMessage(),
                placeDTO
        ));
    }//특정 플레이스 상세 조회



    public ResponseEntity<?> updatePlace(Long placeId, MultipartFile image, PlaceDTO placeDTO, String email) throws IOException {
        // 1. 이메일로 회원 정보 찾기
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. placeId로 Place 조회 (없으면 예외 발생)
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLACE_NOT_FOUND));

        // 3. 현재 로그인한 사용자가 Place의 소유자인지 확인
        if (!place.getUserId().equals(member.getId())) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED_ACCESS);
        }

        // 4. 새 이미지 업로드 (기존 이미지 삭제)
        String imageURL = place.getPlaceImg(); // 기존 이미지 유지
        if (image != null && !image.isEmpty()) {
            if (imageURL != null) { // 기존 이미지 삭제
                awsS3Service.deleteFile(imageURL);
            }
            imageURL = awsS3Service.uploadFile(image); // 새 이미지 업로드
        }

        // 5. 플레이스 정보 업데이트 (널 체크 추가)
        place.updatePlace(
                placeDTO.getPlaceName() != null ? placeDTO.getPlaceName() : place.getPlaceName(),
                placeDTO.getPlaceDetails() != null ? placeDTO.getPlaceDetails() : place.getPlaceDetails(),
                imageURL
        );

        // 6. 저장 및 응답 반환
        placeRepository.save(place);
        return ResponseEntity.status(SuccessStatus._PLACE_UPDATED.getHttpStatus())
                .body(ApiResponse.onSuccess(
                        SuccessStatus._PLACE_UPDATED.getMessage(),
                        place // 업데이트된 플레이스 정보 포함
                ));
    }
//플레이스 수정

    public ResponseEntity<?> toggleBookmark(Long placeId, String email) {
        // 1. 회원 찾기
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 플레이스 찾기
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLACE_NOT_FOUND));

        // 3. 북마크 상태 변경 (토글)
        place.toggleBookmark();
        placeRepository.save(place);

        // 4. 응답 반환
        String message = place.isBookmarked() ? "북마크 추가 완료" : "북마크 삭제 완료";
        return ResponseEntity.status(SuccessStatus._BOOKMARK_UPDATED.getHttpStatus())
                .body(ApiResponse.onSuccess(SuccessStatus._BOOKMARK_UPDATED.getMessage(), message));
    }//플레이스 북마크

}
