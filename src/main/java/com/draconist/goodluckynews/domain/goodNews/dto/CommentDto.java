package com.draconist.goodluckynews.domain.goodNews.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long commentId;  // 댓글 ID
    private Long postId;     // 댓글이 속한 게시글 ID
    private String content;  // 댓글 내용
    private LocalDateTime createdAt; // 작성 날짜
    private int likeCount;   // 댓글 좋아요 개수 추가
    private List<CommentDto> replies; // 답글 리스트 추가
}
