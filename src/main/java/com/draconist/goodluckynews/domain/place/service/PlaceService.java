package com.draconist.goodluckynews.domain.place.service;

import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.domain.place.dto.PlaceCreateDTO;
import com.draconist.goodluckynews.domain.place.dto.PlaceDTO;
import com.draconist.goodluckynews.domain.place.dto.PlacePageResponse;
import com.draconist.goodluckynews.domain.place.entity.Place;
import com.draconist.goodluckynews.domain.place.entity.PlaceLike;
import com.draconist.goodluckynews.domain.place.repository.PlaceLikeRepository;
import com.draconist.goodluckynews.domain.place.repository.PlaceRepository;
import com.draconist.goodluckynews.global.awss3.service.AwsS3Service;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.enums.statuscode.SuccessStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {
    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;
    private final AwsS3Service awsS3Service;
    private final PlaceLikeRepository placeLikeRepository;

    public ResponseEntity<?> createPlace(MultipartFile image, PlaceCreateDTO placeDTO, String email)throws IOException {
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
            awsS3Service.deleteFileByUrl(place.getPlaceImg());
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

    public ResponseEntity<?> findAllWithPagination(int page, int size, String email) {
// 1. 페이지, 사이즈 유효성 검사
        if (page < 0 || size <= 0) {
            throw new GeneralException(ErrorStatus._PAGE_INVALID_REQUEST);
        }
        // 2. 사용자 조회
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        // 3. 페이징 데이터 조회
        Pageable pageable = PageRequest.of(page, size);
        Page<Place> placePage = placeRepository.findAll(pageable);

        List<PlaceDTO> placeDTOList = placePage.getContent().stream()
                .map(place -> {
                    boolean isBookmarked = placeLikeRepository.existsByPlaceIdAndUserId(place.getId(), member.getId());
                    return PlaceDTO.builder()
                            .placeId(place.getId())
                            .placeName(place.getPlaceName())
                            .placeDetails(place.getPlaceDetails())
                            .placeImg(place.getPlaceImg())
                            .likeCount(placeLikeRepository.countByPlaceId(place.getId()))
                            .isBookmark(isBookmarked)
                            .build();
                })
                .collect(Collectors.toList());

        // 4. 조회 결과 없을 때
        if (placeDTOList.isEmpty()) {
            throw new GeneralException(ErrorStatus._PAGE_EMPTY_RESULT);
        }

        PlacePageResponse response = PlacePageResponse.builder()
                .content(placeDTOList)
                .totalPages(placePage.getTotalPages())
                .totalElements(placePage.getTotalElements())
                .pageNumber(placePage.getNumber())
                .pageSize(placePage.getSize())
                .isFirst(placePage.isFirst())
                .isLast(placePage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus._PLACE_PAGINATION_SUCCESS.getMessage(),
                response
        ));
    }

//플레이스 전체 조회 ( 페이지네이션 )

    public ResponseEntity<?> getPlaceById(Long placeId) {
        // 1. placeId로 Place 조회 (없으면 예외 발생)
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLACE_NOT_FOUND));

        // 2. DTO로 변환
        PlaceDTO placeDTO = PlaceDTO.builder()
                .placeId(place.getId())  // 🔹 placeId 추가
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
        //  수정할 값이 하나도 없는 경우 예외 처리
        boolean noUpdateValue =
                (image == null || image.isEmpty()) &&
                        (placeDTO.getPlaceName() == null || placeDTO.getPlaceName().isBlank()) &&
                        (placeDTO.getPlaceDetails() == null || placeDTO.getPlaceDetails().isBlank());

        if (noUpdateValue) {
            throw new GeneralException(ErrorStatus._NO_UPDATE_VALUE);
        }
        // 4. 새 이미지 업로드 (기존 이미지 삭제)
        String imageURL = place.getPlaceImg(); // 기존 이미지 유지
        if (image != null && !image.isEmpty()) {
            if (imageURL != null) { // 기존 이미지 삭제
                awsS3Service.deleteFileByUrl(imageURL);
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

        // 3. 현재 북마크 상태 확인
        boolean isBookmarked = placeLikeRepository.existsByPlaceIdAndUserId(placeId, member.getId());

        String message;
        if (isBookmarked) {
            // 이미 북마크 되어 있으면 삭제
            PlaceLike placeLike = placeLikeRepository.findByPlaceIdAndUserId(placeId, member.getId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus._HEART_NOT_FOUND)); // 적절한 에러 코드 사용
            placeLikeRepository.delete(placeLike);
            message = "북마크 삭제 완료";
        } else {
            // 북마크 안되어 있으면 추가
            PlaceLike newLike = PlaceLike.builder()
                    .place(place)
                    .user(member)
                    .build();
            placeLikeRepository.save(newLike);
            message = "북마크 추가 완료";
        }

        return ResponseEntity.status(SuccessStatus._BOOKMARK_UPDATED.getHttpStatus())
                .body(ApiResponse.onSuccess(SuccessStatus._BOOKMARK_UPDATED.getMessage(), message));
    }
//플레이스 북마크

    public ResponseEntity<?> getMyPlaces(String email, int page, int size) {
        // 1. 사용자 정보 조회
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 페이지네이션 정보 생성
        Pageable pageable = PageRequest.of(page, size);

        // 3. 사용자가 생성한 플레이스 페이징 조회
        Page<Place> placePage = placeRepository.findByUserId(member.getId(), pageable);

        // 4. DTO 변환 및 북마크 정보 추가
        List<PlaceDTO> placeDTOList = placePage.getContent().stream()
                .map(place -> {
                    int likeCount = placeLikeRepository.countByPlaceId(place.getId());
                    boolean isBookmarked = placeLikeRepository.existsByPlaceIdAndUserId(place.getId(), member.getId());
                    return PlaceDTO.builder()
                            .placeId(place.getId())
                            .placeName(place.getPlaceName())
                            .placeDetails(place.getPlaceDetails())
                            .placeImg(place.getPlaceImg())
                            .likeCount(likeCount)
                            .isBookmark(isBookmarked)
                            .build();
                })
                .collect(Collectors.toList());

        // 5. 조회 결과 없을 때 예외 던지기
        if (placeDTOList.isEmpty()) {
            throw new GeneralException(ErrorStatus._PAGE_EMPTY_RESULT);
        }

        // 6. 페이지네이션 정보 포함 응답 생성
        PlacePageResponse response = PlacePageResponse.builder()
                .content(placeDTOList)
                .totalPages(placePage.getTotalPages())
                .totalElements(placePage.getTotalElements())
                .pageNumber(placePage.getNumber())
                .pageSize(placePage.getSize())
                .isFirst(placePage.isFirst())
                .isLast(placePage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus._PLACE_MYLIST_SUCCESS.getMessage(),
                response
        ));
    }

//내가 만든 플레이스 조회



}
