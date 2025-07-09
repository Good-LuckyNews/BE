package com.draconist.goodluckynews.domain.goodNews.dto;

import com.draconist.goodluckynews.domain.goodNews.entity.Post;
import com.draconist.goodluckynews.domain.place.entity.Place;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


public class GoodnewsDto {
    @Data
    @Builder
    public static class GoodnewsCreateDto {
        private Long placeId;
        private String content;
    }//생성 전용 dto

    @Getter
    @Builder
    public static class GoodnewsResponseDto {
        private Long postId;
        private Long placeId;
        private Long userId;
        private String content;
        private String image;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String placeName;

        public static GoodnewsResponseDto from(Post post) {
            return GoodnewsResponseDto.builder()
                    .postId(post.getId())
                    .placeId(post.getPlaceId())
                    .userId(post.getUserId())
                    .content(post.getContent())
                    .image(post.getImage())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .placeName(post.getPlace() != null ? post.getPlace().getPlaceName() : null)
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostDto {
        private Long postId;       // 게시글 ID
        private Long placeId;      // 게시글이 속한 장소 ID
        private Long userId;       // 게시글 작성자 ID
        private String content;    // 게시글 내용
        private String placeName;  // 🔹 플레이스 제목 추가
        private String image;      // 이미지 URL (선택 사항)
        private LocalDateTime createdAt; // 생성 날짜
        private LocalDateTime updatedAt; // 수정 날짜
        private int likeCount;     // 좋아요 개수 추가
        private int commentCount;  // 댓글 개수 추가
    }//값 반환 dto

    @Builder
    @Getter
    public static class PostLikeResponseDto {
        private Long postId;
        private Long placeId;
        private Long userId;
        private int likeCount;
    }
}
