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
@Table(name = "PostLike")
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postLikeId")
    private Long id;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "postId", nullable = false)
    private Long postId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 좋아요 추가 시간

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 좋아요 상태 변경 시간
}
