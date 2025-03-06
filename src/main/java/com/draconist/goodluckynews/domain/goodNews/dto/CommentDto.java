package com.draconist.goodluckynews.domain.goodNews.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long commentId;  // 댓글 ID
    private Long postId;     // 댓글이 속한 게시글 ID
    private Long userId;     // 댓글 작성자 ID
    private String content;  // 댓글 내용
    private LocalDateTime createdAt; // 작성 날짜
    private int likeCount;   // 댓글 좋아요 개수 추가
}
