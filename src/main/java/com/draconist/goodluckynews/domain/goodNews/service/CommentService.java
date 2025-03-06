package com.draconist.goodluckynews.domain.goodNews.service;

import com.draconist.goodluckynews.domain.goodNews.dto.CommentDto;
import com.draconist.goodluckynews.domain.goodNews.entity.Comment;
import com.draconist.goodluckynews.domain.goodNews.entity.CommentLike;
import com.draconist.goodluckynews.domain.goodNews.entity.Post;
import com.draconist.goodluckynews.domain.goodNews.repository.CommentLikeRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.CommentRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.PostRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
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
        // 1. 사용자 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 게시글 조회 (존재 여부 확인)
        Post post = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 3. Comment Entity 변환 및 저장
        Comment comment = Comment.builder()
                .postId(commentDto.getPostId())
                .userId(user.getId())
                .content(commentDto.getContent())
                .build();
        commentRepository.save(comment);

        // 4. 저장된 댓글을 DTO로 변환하여 반환 (likeCount 초기값 0)
        CommentDto responseDto = CommentDto.builder()
                .commentId(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likeCount(0) // 새로 생성된 댓글은 기본적으로 좋아요 0
                .build();

        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<?> getMyComments(String email) {
        // 1. 사용자 조회
        Member user = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

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
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 3. 사용자가 해당 댓글에 좋아요를 눌렀는지 확인
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId);

        if (existingLike.isPresent()) {
            // 좋아요가 이미 존재하면 삭제 (좋아요 취소)
            commentLikeRepository.delete(existingLike.get());
            return ResponseEntity.ok("좋아요 취소됨");
        } else {
            // 좋아요가 없으면 추가
            CommentLike newLike = CommentLike.builder()
                    .userId(user.getId())
                    .commentId(commentId)
                    .build();
            commentLikeRepository.save(newLike);
            return ResponseEntity.ok("좋아요 추가됨");
        }
    }
}
