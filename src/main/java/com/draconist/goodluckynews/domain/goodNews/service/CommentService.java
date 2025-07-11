package com.draconist.goodluckynews.domain.goodNews.service;

import com.draconist.goodluckynews.domain.goodNews.dto.CommentDto;
import com.draconist.goodluckynews.domain.goodNews.dto.GoodnewsDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
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

    // 댓글 생성
    public ResponseEntity<?> createComment(Long postId, CommentDto.CommentCreateDto commentDto, String email) {
        // 1. 회원 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 게시글 조회
        postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));

        // 3. 댓글 저장
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(user.getId())
                .content(commentDto.getContent())
                .build();
        commentRepository.save(comment);

        // 4. 성공 응답
        return ResponseEntity.status(SuccessStatus.COMMENT_CREATED.getHttpStatus())
                .body(ApiResponse.onSuccess(
                        SuccessStatus.COMMENT_CREATED.getMessage(),
                        comment
                ));
    }

    // 특정 게시글의 댓글 목록
    public ResponseEntity<?> getCommentsByPost(Long postId, int page, int size) {
        // 1. 게시글 존재 체크
        postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));

        // 2. 댓글 목록 조회
        List<Comment> comments = commentRepository.findByPostId(postId);

        List<CommentDto.CommentResultDto> commentDtoList = comments.stream()
                .map(comment -> {
                    List<Comment> replies = commentRepository.findByParentCommentId(comment.getId());

                    List<CommentDto.CommentResultDto> replyDtoList = replies.stream()
                            .map(reply -> CommentDto.CommentResultDto.builder()
                                    .commentId(reply.getId())
                                    .postId(reply.getPostId())
                                    .content(reply.getContent())
                                    .createdAt(reply.getCreatedAt())
                                    .likeCount(commentLikeRepository.countByCommentId(reply.getId()))
                                    .replies(null)
                                    .build())
                            .collect(Collectors.toList());

                    return CommentDto.CommentResultDto.builder()
                            .commentId(comment.getId())
                            .postId(comment.getPostId())
                            .content(comment.getContent())
                            .createdAt(comment.getCreatedAt())
                            .likeCount(commentLikeRepository.countByCommentId(comment.getId()))
                            .replies(replyDtoList)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(SuccessStatus.COMMENT_LIST_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(true, SuccessStatus.COMMENT_LIST_SUCCESS, commentDtoList));
    }


    //사용자의 댓글 조회
    public ResponseEntity<?> getMyComments(String email) {
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<Comment> userComments = commentRepository.findByUserId(member.getId());

        List<CommentDto.CommentResultDto> commentDtoList = userComments.stream()
                .map(comment -> CommentDto.CommentResultDto.builder()
                        .commentId(comment.getId())
                        .postId(comment.getPostId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .likeCount(commentLikeRepository.countByCommentId(comment.getId()))
                        .replies(null) // 내 댓글 목록에서는 대댓글까지 보여주지 않는다면 null 또는 빈 리스트
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.COMMENT_LIST_SUCCESS.getMessage(),
                commentDtoList
        ));
    }





    // 댓글 좋아요 토글
    public ResponseEntity<?> toggleCommentLike(Long postId, Long commentId, String email) {
        // 0. 게시글 존재 체크
        postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));

        // 1. 사용자 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));

        // 3. 사용자가 해당 댓글에 좋아요를 눌렀는지 확인
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId);

        boolean liked;
        SuccessStatus resultStatus;

        if (existingLike.isPresent()) {
            // 좋아요가 이미 존재하면 삭제 (좋아요 취소)
            commentLikeRepository.delete(existingLike.get());
            liked = false;
            resultStatus = SuccessStatus.COMMENT_LIKE_REMOVED;
        } else {
            // 좋아요가 없으면 추가
            CommentLike newLike = CommentLike.builder()
                    .userId(user.getId())
                    .commentId(commentId)
                    .build();
            commentLikeRepository.save(newLike);
            liked = true;
            resultStatus = SuccessStatus.COMMENT_LIKE_ADDED;
        }

        // 결과 반환
        return ResponseEntity.status(resultStatus.getHttpStatus())
                .body(ApiResponse.onSuccess(
                        resultStatus.getMessage(),
                        Map.of(
                                "commentId", commentId,
                                "liked", liked
                        )
                ));
    }



    //댓글 삭제
    public ResponseEntity<?> deleteComment(Long postId,Long commentId, String email) {
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
    }

    //댓글에 대한 대댓글 작성하기
    // 대댓글 작성
    public ResponseEntity<?> createReplyToComment(Long postId, CommentDto.CommentCreateDto commentDto, String email, Long parentCommentId) {
        try {
            Member user = memberRepository.findMemberByEmail(email)
                    .orElseThrow(() -> new RuntimeException(ErrorStatus.MEMBER_NOT_FOUND.getMessage()));

            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException(ErrorStatus.COMMENT_NOT_FOUND.getMessage()));

            Comment replyComment = Comment.builder()
                    .postId(parentComment.getPostId())
                    .userId(user.getId())
                    .content(commentDto.getContent())
                    .parentComment(parentComment)
                    .build();

            commentRepository.save(replyComment);

            return ResponseEntity.status(SuccessStatus.COMMENT_REPLIES_CREATED.getHttpStatus())
                    .body(ApiResponse.onSuccess(
                            SuccessStatus.COMMENT_REPLIES_CREATED.getMessage(),
                            replyComment
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(ErrorStatus.COMMENT_CREATION_FAILED.getHttpStatus())
                    .body(ApiResponse.onFailure(
                            ErrorStatus.COMMENT_CREATION_FAILED.getCode(),
                            ErrorStatus.COMMENT_CREATION_FAILED.getMessage(),
                            null
                    ));
        }
    }


}
