package com.draconist.goodluckynews.domain.goodNews.service;

import com.draconist.goodluckynews.domain.goodNews.dto.GoodnewsDto;
import com.draconist.goodluckynews.domain.goodNews.entity.Post;
import com.draconist.goodluckynews.domain.goodNews.entity.PostLike;
import com.draconist.goodluckynews.domain.goodNews.repository.CommentRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.PostRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.PostLikeRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.domain.place.entity.Place;
import com.draconist.goodluckynews.domain.place.repository.PlaceRepository;
import com.draconist.goodluckynews.global.awss3.service.AwsS3Service;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.enums.statuscode.SuccessStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final AwsS3Service awsS3Service;
    private final PlaceRepository placeRepository;

    public ResponseEntity<?> createPost(GoodnewsDto.GoodnewsCreateDto goodnewsCreateDTO, MultipartFile image, String email) {
        try {
            Member user = memberRepository.findMemberByEmail(email)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

            Place place = placeRepository.findById(goodnewsCreateDTO.getPlaceId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.PLACE_NOT_FOUND));

            String imageUrl = Optional.ofNullable(image)
                    .filter(f -> !f.isEmpty())
                    .map(f -> awsS3Service.uploadFile(f))
                    .orElse(null);

            Post post = Post.builder()
                    .placeId(place.getId())
                    .userId(user.getId())
                    .content(goodnewsCreateDTO.getContent())
                    .image(imageUrl)
                    .build();

            post.setPlace(place);

            postRepository.save(post);

            // DTO 변환 및 반환
            GoodnewsDto.GoodnewsResponseDto response = GoodnewsDto.GoodnewsResponseDto.from(post);

            return ResponseEntity.status(SuccessStatus.POST_CREATED.getHttpStatus())
                    .body(ApiResponse.onSuccess(SuccessStatus.POST_CREATED.getMessage(), response));
        } catch (Exception e) {
            return ResponseEntity.status(ErrorStatus.POST_CREATION_FAILED.getHttpStatus())
                    .body(ErrorStatus.POST_CREATION_FAILED);
        }
    }//게시글 생성

    public ResponseEntity<?> getPostById(Long postId) {
        // 1. 게시글 조회 (없으면 예외 발생)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(ErrorStatus.POST_NOT_FOUND.getMessage()));


        // 2. 조회된 게시글을 DTO로 변환하여 반환
        GoodnewsDto.PostDto postDto = GoodnewsDto.PostDto.builder()
                .postId(post.getId())
                .placeId(post.getPlaceId())
                .userId(post.getUserId())
                .content(post.getContent())
                .placeName(post.getPlace() != null ? post.getPlace().getPlaceName() : null)
                .image(post.getImage())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likeCount(postLikeRepository.countByPostId(post.getId()))
                .commentCount(commentRepository.countByPostId(post.getId()))
                .build();

        return ResponseEntity.ok(postDto);
    }

    public ResponseEntity<?> getAllPosts(int page, int size) {
        // 1. 페이지네이션 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        // 2. 페이지네이션 적용하여 게시글 조회
        Page<Post> postPage = postRepository.findAll(pageable);

        // 3. 조회된 게시글을 DTO로 변환하여 리스트로 반환
        List<GoodnewsDto.PostDto> postDtoList = postPage.getContent().stream()
                .map(post -> GoodnewsDto.PostDto.builder()
                        .postId(post.getId())
                        .placeId(post.getPlaceId())
                        .userId(post.getUserId())
                        .content(post.getContent())
                        .placeName(post.getPlace() != null ? post.getPlace().getPlaceName() : null)
                        .image(post.getImage())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .likeCount(postLikeRepository.countByPostId(post.getId()))
                        .commentCount(commentRepository.countByPostId(post.getId()))
                        .build())
                .collect(Collectors.toList());

        // 4. 응답 데이터 생성
        return ResponseEntity.status(SuccessStatus.POST_LIST_SUCCESS.getHttpStatus())
                .body(ApiResponse.onSuccess(
                        SuccessStatus.POST_LIST_SUCCESS.getMessage(),
                        postDtoList
                ));
    }

    public ResponseEntity<?> togglePostLike(Long postId, String email) {
        // 1. 사용자 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 3. 사용자가 해당 게시글에 좋아요를 눌렀는지 확인
        Optional<PostLike> existingLike = postLikeRepository.findByUserIdAndPostId(user.getId(), postId);

        String message;
        if (existingLike.isPresent()) {
            // 좋아요가 이미 존재하면 삭제 (좋아요 취소)
            postLikeRepository.delete(existingLike.get());
            message = "좋아요 취소됨";
        } else {
            // 좋아요가 없으면 추가
            PostLike newLike = PostLike.builder()
                    .userId(user.getId())
                    .postId(postId)
                    .build();
            postLikeRepository.save(newLike);
            message = "좋아요 생성됨";
        }

        // 현재 좋아요 개수 조회
        int likeCount = (int) postLikeRepository.countByPostId(postId);

        // DTO 생성
        GoodnewsDto.PostLikeResponseDto responseDto = GoodnewsDto.PostLikeResponseDto.builder()
                .postId(post.getId())
                .placeId(post.getPlaceId())
                .userId(user.getId())
                .likeCount(likeCount)
                .build();

        // 메시지와 DTO를 함께 반환
        return ResponseEntity.ok(
                ApiResponse.onSuccess(message, responseDto)
        );
    }


    public ResponseEntity<?> searchPostsByContent(String query) {
        List<Post> searchResults = postRepository.searchByContent(query);

        List<GoodnewsDto.PostDto> postDtoList = searchResults.stream()
                .map(post -> GoodnewsDto.PostDto.builder()
                        .postId(post.getId())
                        .placeId(post.getPlaceId())
                        .userId(post.getUserId())
                        .content(post.getContent())
                        .placeName(post.getPlace() != null ? post.getPlace().getPlaceName() : null)
                        .image(post.getImage())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .likeCount(postLikeRepository.countByPostId(post.getId()))
                        .commentCount(commentRepository.countByPostId(post.getId()))
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.status(SuccessStatus.POST_LIST_SUCCESS.getHttpStatus())
                .body(ApiResponse.onSuccess(
                        SuccessStatus.POST_LIST_SUCCESS.getMessage(),
                        postDtoList
                ));
    }


    public ResponseEntity<?> getMyPosts(String email) {
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Post> posts = postRepository.findByUserIdWithPlace(user.getId());

        List<GoodnewsDto.PostDto> postDtoList = posts.stream()
                .map(post -> GoodnewsDto.PostDto.builder()
                        .postId(post.getId())
                        .placeId(post.getPlaceId())
                        .placeName(post.getPlace().getPlaceName())  // 🔹 플레이스 제목 추가
                        .userId(post.getUserId())
                        .content(post.getContent())
                        .image(post.getImage())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .likeCount(postLikeRepository.countByPostId(post.getId()))
                        .commentCount(commentRepository.countByPostId(post.getId()))
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.status(SuccessStatus.POST_LIST_SUCCESS.getHttpStatus())
                .body(ApiResponse.onSuccess(
                        SuccessStatus.POST_LIST_SUCCESS.getMessage(),
                        postDtoList
                ));
    }

    @Transactional
    public ResponseEntity<?> deletePost(Long postId, String email) {
        // 1. 사용자 정보 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 게시글 조회 (없으면 예외 발생)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));

        // 3. 현재 로그인한 사용자가 게시글 작성자인지 확인
        if (!post.getUserId().equals(user.getId())) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED_ACCESS);
        }

        // 4. 해당 게시글의 좋아요 & 댓글 삭제
        postLikeRepository.deleteByPostId(postId);
        commentRepository.deleteByPostId(postId);

        // 5. 게시글 삭제
        postRepository.delete(post);

        // 6. 성공 응답 반환
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.POST_DELETED.getMessage(),
                "게시글이 성공적으로 삭제되었습니다."
        ));
    }//희소식 삭제

    public List<GoodnewsDto.PostDto> getPostsByPlace(Long placeId) {
        // 1. 플레이스 존재 여부 확인
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("해당 플레이스를 찾을 수 없습니다."));

        // 2. 플레이스에 속한 게시글 조회
        return postRepository.findByPlaceIdOrderByCreatedAtDesc(placeId)
                .stream()
                .map(post -> GoodnewsDto.PostDto.builder()
                        .postId(post.getId())
                        .placeId(post.getPlaceId())
                        .placeName(place.getPlaceName())  // 🔹 플레이스명 추가
                        .content(post.getContent())
                        .image(post.getImage())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .likeCount(postLikeRepository.countByPostId(post.getId()))
                        .commentCount(commentRepository.countByPostId(post.getId()))
                        .build())
                .collect(Collectors.toList());
    }//플레이스별 희소식 조회

}
