package com.draconist.goodluckynews.domain.goodNews.dto;

import com.draconist.goodluckynews.domain.member.dto.WriterInfoDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


public class CommentDto {
    @Getter
    @Setter
    @Builder
    public static class CommentCreateDto{
        private String content;  // 댓글 내용
    }

    @Getter
    @Setter
    @Builder
    public static class CommentResultDto{
        private Long commentId;  // 댓글 ID
        private Long postId;     // 댓글이 속한 게시글 ID
        private String content;  // 댓글 내용
        private LocalDateTime createdAt; // 작성 날짜
        private int likeCount;   // 댓글 좋아요 개수 추가
        private List<CommentResultDto> replies;// 답글 리스트 추가
        private WriterInfoDto writer; // 작성자 정보 추가하기

    }

}
