package com.draconist.goodluckynews.domain.goodNews.service;

import com.draconist.goodluckynews.domain.goodNews.dto.CommentDto;
import com.draconist.goodluckynews.domain.goodNews.entity.Comment;
import com.draconist.goodluckynews.domain.goodNews.entity.CommentLike;
import com.draconist.goodluckynews.domain.goodNews.repository.CommentLikeRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.CommentRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.PostRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import com.draconist.goodluckynews.global.enums.statuscode.ErrorStatus;
import com.draconist.goodluckynews.global.enums.statuscode.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
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
        // 1. 사용자 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new RuntimeException(ErrorStatus.MEMBER_NOT_FOUND.getMessage()));

        // 2. 사용자가 작성한 모든 댓글 조회
        List<Comment> comments = commentRepository.findByUserId(user.getId());

        // 3. 조회된 댓글을 DTO로 변환하여 리스트로 반환
        List<CommentDto> commentDtoList = comments.stream()
                .map(comment -> CommentDto.builder()
                        .commentId(comment.getId())
                        .postId(comment.getPostId())
                        .userId(comment.getUserId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .likeCount(commentLikeRepository.countByCommentId(comment.getId())) // 좋아요 개수 추가
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(commentDtoList);
    }

    public ResponseEntity<?> getAllComments() {
        // 1. 모든 댓글 조회
        List<Comment> comments = commentRepository.findAll();

        // 2. 조회된 댓글을 DTO로 변환하여 리스트로 반환
        List<CommentDto> commentDtoList = comments.stream()
                .map(comment -> CommentDto.builder()
                        .commentId(comment.getId())
                        .postId(comment.getPostId())
                        .userId(comment.getUserId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .likeCount(commentLikeRepository.countByCommentId(comment.getId())) // 좋아요 개수 추가
                        .build())
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
}
