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
import com.draconist.goodluckynews.domain.member.dto.WriterInfoDto;
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
import org.springframework.transaction.annotation.Transactional;
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


    private WriterInfoDto mapToWriterDto(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return WriterInfoDto.builder()
                .userId(member.getId())
                .name(member.getName())
                .profileImage(member.getProfileImage())
                .build();
    } //작성자 매핑 함수 추가함


    private CommentDto.CommentResultDto toSingleCommentDto(Comment comment) {
        return CommentDto.CommentResultDto.builder()
                .commentId(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likeCount(commentLikeRepository.countByCommentId(comment.getId()))
                .writer(mapToWriterDto(comment.getUserId()))
                .replies(Collections.emptyList()) // 생성 시점에는 대댓글 없음
                .build();
    }//대댓글 없을 때


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
                        toSingleCommentDto(comment)
                ));

    }

    // 특정 게시글의 댓글 목록
    public ResponseEntity<?> getCommentsByPost(Long postId, int page, int size) {
        // 1. 게시글 존재 체크
        postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));

        // 2. 최상위 댓글만 조회 (parentComment == null)
        List<Comment> topLevelComments = commentRepository.findByPostIdAndParentCommentIsNull(postId);

        // 3. 트리 구조로 replies까지 구성
        List<CommentDto.CommentResultDto> commentDtoList = topLevelComments.stream()
                .map(this::toCommentResultDtoWithReplies)
                .collect(Collectors.toList());

        return ResponseEntity.status(SuccessStatus.COMMENT_LIST_SUCCESS.getHttpStatus())
                .body(ApiResponse.of(true, SuccessStatus.COMMENT_LIST_SUCCESS, commentDtoList));
    }

    // 재귀적으로 replies를 구성
    private CommentDto.CommentResultDto toCommentResultDtoWithReplies(Comment comment) {
        List<Comment> replies = commentRepository.findByParentCommentId(comment.getId());
        List<CommentDto.CommentResultDto> replyDtoList = replies.stream()
                .map(this::toCommentResultDtoWithReplies)
                .collect(Collectors.toList());

        return CommentDto.CommentResultDto.builder()
                .commentId(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likeCount(commentLikeRepository.countByCommentId(comment.getId()))
                .writer(mapToWriterDto(comment.getUserId()))//작성자 정보 추가
                .replies(replyDtoList)
                .build();
    }



    //사용자의 댓글 조회
    public ResponseEntity<?> getMyComments(String email) {
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 1. 내 댓글 중 최상위(부모 없는) 댓글만 조회
        List<Comment> topLevelComments = commentRepository.findByUserIdAndParentCommentIsNull(member.getId());

        // 2. 트리 구조로 replies까지 구성
        List<CommentDto.CommentResultDto> commentDtoList = topLevelComments.stream()
                .map(comment -> toMyCommentResultDtoWithReplies(comment, member.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.COMMENT_LIST_SUCCESS.getMessage(),
                commentDtoList
        ));
    }

    // 재귀적으로 replies를 구성 (내가 쓴 대댓글만)
    private CommentDto.CommentResultDto toMyCommentResultDtoWithReplies(Comment comment, Long userId) {
        List<Comment> replies = commentRepository.findByUserIdAndParentCommentId(userId, comment.getId());
        List<CommentDto.CommentResultDto> replyDtoList = replies.stream()
                .map(reply -> toMyCommentResultDtoWithReplies(reply, userId))
                .collect(Collectors.toList());

        //Post, Place 접근 (플레이스 이미지 가져와야함)
        Post post = comment.getPost();
        Place place = post !=null? post.getPlace() : null;

        return CommentDto.CommentResultDto.builder()
                .commentId(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likeCount(commentLikeRepository.countByCommentId(comment.getId()))
                .writer(mapToWriterDto(comment.getUserId())) //작성자 정보 추가
                .replies(replyDtoList)
                .placeImg(place != null ? place.getPlaceImg() : null) // 플레이스 이미지 추가
                .placeName(place != null ? place.getPlaceName() : null) // 플레이스 이름 추가
                .build();
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
    @Transactional
    public ResponseEntity<?> deleteComment(Long postId, Long commentId, String email) {
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

        // 4. 자식 댓글 존재 여부 확인
        boolean hasChild = commentRepository.existsByParentCommentId(commentId);

        // 5. 좋아요 삭제 (부모 댓글)
        commentLikeRepository.deleteByCommentId(commentId);

        if (hasChild) {
            // 6. 소프트 딜리트: 내용만 "삭제된 댓글입니다" 등으로 변경
            comment.setContent("삭제된 댓글입니다.");
            comment.setDeleted(true);
            commentRepository.save(comment);
        } else {
            // 7. 실제 삭제
            commentRepository.delete(comment);
        }

        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.COMMENT_DELETED.getMessage(),
                "댓글이 성공적으로 삭제되었습니다."
        ));
    }



    //댓글에 대한 대댓글 작성하기
    public ResponseEntity<?> createReplyToComment(Long postId, CommentDto.CommentCreateDto commentDto, String email, Long parentCommentId) {
        // 1. 게시글 존재 체크
        postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));

        // 2. 회원 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 3. 부모 댓글 조회
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));

        // 4. 대댓글 생성
        Comment replyComment = Comment.builder()
                .postId(parentComment.getPostId())
                .userId(user.getId())
                .content(commentDto.getContent())
                .parentComment(parentComment)
                .build();

        commentRepository.save(replyComment);

        // 5. 성공 응답
        return ResponseEntity.status(SuccessStatus.COMMENT_REPLIES_CREATED.getHttpStatus())
                .body(ApiResponse.onSuccess(
                        SuccessStatus.COMMENT_REPLIES_CREATED.getMessage(),
                        toSingleCommentDto(replyComment)
                ));

    }


}
