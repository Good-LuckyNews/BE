package com.draconist.goodluckynews.domain.goodNews.service;

import com.draconist.goodluckynews.domain.goodNews.dto.CommentDto;
import com.draconist.goodluckynews.domain.goodNews.entity.Comment;
import com.draconist.goodluckynews.domain.goodNews.entity.Post;
import com.draconist.goodluckynews.domain.goodNews.repository.CommentRepository;
import com.draconist.goodluckynews.domain.goodNews.repository.PostRepository;
import com.draconist.goodluckynews.domain.member.entity.Member;
import com.draconist.goodluckynews.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
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

        // 4. 저장된 댓글을 DTO로 변환하여 반환
        CommentDto responseDto = CommentDto.builder()
                .commentId(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();

        return ResponseEntity.ok(responseDto);
    }//댓글 생성

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
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(commentDtoList);
    }//

    public ResponseEntity<?> getAllComments(String email) {





    }//댓글 전체 조회
}
