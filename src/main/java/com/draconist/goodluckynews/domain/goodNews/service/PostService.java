package com.draconist.goodluckynews.domain.goodNews.service;

import com.draconist.goodluckynews.domain.goodNews.dto.PostDto;
import com.draconist.goodluckynews.domain.goodNews.entity.Post;
import com.draconist.goodluckynews.domain.goodNews.entity.PostLike;
import com.draconist.goodluckynews.domain.goodNews.repository.PostRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.PostLikeRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.enums.statuscode.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostLikeRepository postLikeRepository;

    public ResponseEntity<?> createPost(PostDto postDto, String email) {
        try {
            Member user = memberRepository.findMemberByEmail(email)
                    .orElseThrow(() -> new RuntimeException(ErrorStatus.MEMBER_NOT_FOUND.getMessage()));

            Post post = Post.createPost(postDto.getTitle(), postDto.getPlaceId(), user.getId(), postDto.getContent(), postDto.getImage());
            postRepository.save(post);

            return ResponseEntity.status(SuccessStatus.POST_CREATED.getHttpStatus()).body(SuccessStatus.POST_CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(ErrorStatus.POST_CREATION_FAILED.getHttpStatus())
                    .body(ErrorStatus.POST_CREATION_FAILED);
        }
    }

    public ResponseEntity<?> getPostById(Long postId) {
        // 1. 게시글 조회 (없으면 예외 발생)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(ErrorStatus.POST_NOT_FOUND.getMessage()));


        // 2. 조회된 게시글을 DTO로 변환하여 반환
        PostDto postDto = PostDto.builder()
                .postId(post.getId())
                .title(post.getTitle())  // 제목 추가
                .placeId(post.getPlaceId())
                .userId(post.getUserId())
                .content(post.getContent())
                .image(post.getImage())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();

        return ResponseEntity.ok(postDto);
    }

    public ResponseEntity<?> getAllPosts(int page, int size) {
        // 1. 페이지네이션 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        // 2. 페이지네이션 적용하여 게시글 조회
        Page<Post> postPage = postRepository.findAll(pageable);

        // 3. 조회된 게시글을 DTO로 변환하여 리스트로 반환
        List<PostDto> postDtoList = postPage.getContent().stream()
                .map(post -> PostDto.builder()
                        .postId(post.getId())  // 전체 조회에서 받은 ID를 상세 조회에 사용 가능
                        .title(post.getTitle())  // 제목 추가
                        .placeId(post.getPlaceId())
                        .userId(post.getUserId())
                        .content(post.getContent())
                        .image(post.getImage())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        // 4. 응답 데이터 생성
        return ResponseEntity.status(SuccessStatus.POST_LIST_SUCCESS.getHttpStatus()).body(postDtoList);
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

        if (existingLike.isPresent()) {
            // 좋아요가 이미 존재하면 삭제 (좋아요 취소)
            postLikeRepository.delete(existingLike.get());
            return ResponseEntity.ok("좋아요 취소됨");
        } else {
            // 좋아요가 없으면 추가
            PostLike newLike = PostLike.builder()
                    .userId(user.getId())
                    .postId(postId)
                    .build();
            postLikeRepository.save(newLike);
            return ResponseEntity.ok("좋아요 추가됨");
        }
    }

    public ResponseEntity<?> searchPostsByTitle(String query) {
        // 1. 검색 실행
        List<Post> searchResults = postRepository.searchByTitle(query);

        // 2. 조회된 게시글을 DTO로 변환하여 리스트로 반환
        List<PostDto> postDtoList = searchResults.stream()
                .map(post -> PostDto.builder()
                        .postId(post.getId())
                        .title(post.getTitle())  // 제목 추가
                        .placeId(post.getPlaceId())
                        .userId(post.getUserId())
                        .content(post.getContent())
                        .image(post.getImage())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.status(SuccessStatus.POST_LIST_SUCCESS.getHttpStatus()).body(postDtoList);
    }

    public ResponseEntity<?> getMyPosts(String email) {
        // 1. 사용자 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 사용자가 작성한 모든 게시글 조회
        List<Post> posts = postRepository.findByUserId(user.getId());

        // 3. 조회된 게시글을 DTO로 변환하여 리스트로 반환
        List<PostDto> postDtoList = posts.stream()
                .map(post -> PostDto.builder()
                        .postId(post.getId())
                        .title(post.getTitle())
                        .placeId(post.getPlaceId())
                        .userId(post.getUserId())
                        .content(post.getContent())
                        .image(post.getImage())
                        .createdAt(post.getCreatedAt())
                        .updatedAt(post.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.status(SuccessStatus.POST_LIST_SUCCESS.getHttpStatus()).body(postDtoList);
    }


}
