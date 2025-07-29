package com.draconist.goodluckynews.domain.goodNews.entity;

import com.draconist.goodluckynews.domain.place.entity.Place;
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

    @ManyToOne(fetch = FetchType.LAZY)  // 연관관계 매핑 (지연 로딩)
    @JoinColumn(name = "placeId", insertable = false, updatable = false)  // 🔹 placeId를 외래키로 사용
    private Place place;  // 🔹 플레이스 엔티티 참조

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
}
