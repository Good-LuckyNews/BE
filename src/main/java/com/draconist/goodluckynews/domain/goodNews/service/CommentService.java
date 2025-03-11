package com.draconist.goodluckynews.domain.goodNews.service;

import com.draconist.goodluckynews.domain.goodNews.dto.CommentDto;
import com.draconist.goodluckynews.domain.goodNews.dto.PostDto;
import com.draconist.goodluckynews.domain.goodNews.entity.Comment;
import com.draconist.goodluckynews.domain.goodNews.entity.CommentLike;
import com.draconist.goodluckynews.domain.goodNews.entity.Post;
import com.draconist.goodluckynews.domain.goodNews.repository.CommentLikeRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.CommentRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.PostLikeRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.PostRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.domain.place.entity.Place;
import com.draconist.goodluckynews.domain.place.repository.PlaceRepository;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.enums.statuscode.SuccessStatus;
import com.draconist.goodluckynews.global.exception.GeneralException;
import com.draconist.goodluckynews.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;

    public ResponseEntity<?> createComment(CommentDto commentDto, String email) {
        try {
            Member user = memberRepository.findMemberByEmail(email)
                    .orElseThrow(() -> new RuntimeException(ErrorStatus.MEMBER_NOT_FOUND.getMessage()));

            postRepository.findById(commentDto.getPostId())
                    .orElseThrow(() -> new RuntimeException(ErrorStatus.POST_NOT_FOUND.getMessage()));

            Comment comment = Comment.builder()
                    .postId(commentDto.getPostId())
                    .userId(user.getId())
                    .content(commentDto.getContent())
                    .build();
            commentRepository.save(comment);

            return ResponseEntity.status(SuccessStatus.COMMENT_CREATED.getHttpStatus()).body(SuccessStatus.COMMENT_CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(ErrorStatus.COMMENT_CREATION_FAILED.getHttpStatus())
                    .body(ErrorStatus.COMMENT_CREATION_FAILED);
        }
    }


    public ResponseEntity<?> getMyComments(String email) {
        // 1. 사용자 정보 조회
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 사용자가 작성한 댓글 목록 조회
        List<Comment> userComments = commentRepository.findByUserId(member.getId());

        // 3. 댓글이 달린 게시글 ID 목록 추출 (중복 제거)
        Set<Long> postIds = userComments.stream()
                .map(Comment::getPostId)
                .collect(Collectors.toSet());

        // 4. 해당 게시글 목록 조회
        List<Post> posts = postRepository.findByIdIn(postIds);

        // 5. 게시글과 플레이스를 DTO로 변환하여 반환
        List<PostDto> postDtoList = posts.stream()
                .map(post -> {
                    Place place = placeRepository.findById(post.getPlaceId()).orElse(null);
                    return PostDto.builder()
                            .postId(post.getId())
                            .placeId(post.getPlaceId())
                            .placeName(place != null ? place.getPlaceName() : "알 수 없음") // 플레이스 이름 추가
                            .content(post.getContent())
                            .image(post.getImage())
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .likeCount(postLikeRepository.countByPostId(post.getId())) // 좋아요 개수 추가
                            .commentCount(commentRepository.countByPostId(post.getId())) // 댓글 개수 추가
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.POST_LIST_SUCCESS.getMessage(),
                postDtoList
        ));
    }



    public ResponseEntity<?> getAllComments() {
        // 1. 모든 댓글 조회
        List<Comment> comments = commentRepository.findAll();

        // 2. 각 댓글에 대한 답글 조회
        List<CommentDto> commentDtoList = comments.stream()
                .map(comment -> {
                    // 댓글에 달린 답글 조회
                    List<Comment> replies = commentRepository.findByParentCommentId(comment.getId());

                    // 답글 리스트를 CommentDto로 변환
                    List<CommentDto> replyDtoList = replies.stream()
                            .map(reply -> CommentDto.builder()
                                    .commentId(reply.getId())
                                    .postId(reply.getPostId())
                                    .content(reply.getContent())
                                    .createdAt(reply.getCreatedAt())
                                    .likeCount(commentLikeRepository.countByCommentId(reply.getId())) // 좋아요 개수
                                    .build())
                            .collect(Collectors.toList());

                    // 댓글을 CommentDto로 변환 후, 답글 리스트 포함
                    return CommentDto.builder()
                            .commentId(comment.getId())
                            .postId(comment.getPostId())
                            .content(comment.getContent())
                            .createdAt(comment.getCreatedAt())
                            .likeCount(commentLikeRepository.countByCommentId(comment.getId())) // 좋아요 개수
                            .replies(replyDtoList) // 답글 포함
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(commentDtoList);
    }


    public ResponseEntity<?> toggleCommentLike(Long commentId, String email) {
        // 1. 사용자 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new RuntimeException(ErrorStatus.MEMBER_NOT_FOUND.getMessage()));

        // 2. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException(ErrorStatus.COMMENT_NOT_FOUND.getMessage()));

        // 3. 사용자가 해당 댓글에 좋아요를 눌렀는지 확인
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId);

        if (existingLike.isPresent()) {
            // 좋아요가 이미 존재하면 삭제 (좋아요 취소)
            commentLikeRepository.delete(existingLike.get());
            return ResponseEntity.ok(SuccessStatus.COMMENT_LIKE_SUCCESS);
        } else {
            // 좋아요가 없으면 추가
            CommentLike newLike = CommentLike.builder()
                    .userId(user.getId())
                    .commentId(commentId)
                    .build();
            commentLikeRepository.save(newLike);
            return ResponseEntity.ok(SuccessStatus.COMMENT_LIKE_SUCCESS);
        }
    }


    public ResponseEntity<?> deleteComment(Long commentId, String email) {
        // 1. 사용자 정보 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 댓글 조회 (없으면 예외 발생)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));

        // 3. 현재 로그인한 사용자가 댓글 작성자인지 확인
        if (!comment.getUserId().equals(user.getId())) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED_ACCESS);
        }

        // 4. 해당 댓글의 좋아요 삭제
        commentLikeRepository.deleteByCommentId(commentId);

        // 5. 댓글 삭제
        commentRepository.delete(comment);

        // 6. 성공 응답 반환
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.COMMENT_DELETED.getMessage(),
                "댓글이 성공적으로 삭제되었습니다."
        ));
    }//댓글 삭제

    public ResponseEntity<?> createReplyToComment(CommentDto commentDto, String email, Long commentId) {
        try {
            // 1. 사용자 정보 조회
            Member user = memberRepository.findMemberByEmail(email)
                    .orElseThrow(() -> new RuntimeException(ErrorStatus.MEMBER_NOT_FOUND.getMessage()));

            // 2. 댓글 조회 (답글을 달기 위한 부모 댓글)
            Comment parentComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException(ErrorStatus.COMMENT_NOT_FOUND.getMessage()));

            // 3. 답글 생성
            Comment replyComment = Comment.builder()
                    .postId(parentComment.getPostId())  // 부모 댓글과 동일한 게시글에 답글 추가
                    .userId(user.getId())
                    .content(commentDto.getContent())
                    .parentComment(parentComment)  // 부모 댓글 설정
                    .build();

            // 4. 댓글 저장
            commentRepository.save(replyComment);

            return ResponseEntity.status(SuccessStatus.COMMENT_CREATED.getHttpStatus()).body(SuccessStatus.COMMENT_CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(ErrorStatus.COMMENT_CREATION_FAILED.getHttpStatus())
                    .body(ErrorStatus.COMMENT_CREATION_FAILED);
        }
    }//댓글에 대한 답글 작성하기

    public ResponseEntity<?> commentAlarm(String email) {
        // 1. 사용자 정보 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 사용자가 작성한 댓글을 조회
        List<Comment> userComments = commentRepository.findByUserId(user.getId());

        // 3. 사용자가 작성한 댓글에 달린 대댓글들을 찾기 위한 리스트
        List<Comment> repliesToUserComments = new ArrayList<>();

        // 4. 사용자가 작성한 댓글에 대해 대댓글이 달린 것들만 찾기
        for (Comment comment : userComments) {
            List<Comment> replies = commentRepository.findByParentComment(comment);
            repliesToUserComments.addAll(replies);
        }

        // 5. 대댓글이 존재할 경우 처리
        if (!repliesToUserComments.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.onSuccess(
                    SuccessStatus.COMMENT_REPLIES_FOUND.getMessage(),
                    repliesToUserComments
            ));
        }

        // 6. 대댓글이 없으면 메시지 반환
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.NO_REPLIES_FOUND.getMessage(),
                "사용자가 작성한 댓글에 달린 대댓글이 없습니다."
        ));
    } //내 댓글에 달린 대댓글 찾기

}
