package com.draconist.goodluckynews.domain.goodNews.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postId")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title; // 게시글 제목

    @Column(name = "placeId", nullable = false)
    private Long placeId; // 게시글이 속한 장소 ID

    @Column(name = "userId", nullable = false)
    private Long userId; // 게시글 작성자 ID

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 게시글 내용

    @Column(name = "image", columnDefinition = "TEXT")
    private String image; // 게시글 이미지 (선택 사항)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 생성 날짜

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 수정 날짜

    public static Post createPost(String title, Long placeId, Long userId, String content, String image) {
        return Post.builder()
                .title(title) // 제목 추가
                .placeId(placeId)
                .userId(userId)
                .content(content)
                .image(image)
                .build();
    }
}
