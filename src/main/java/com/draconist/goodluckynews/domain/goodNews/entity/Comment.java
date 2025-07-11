package com.draconist.goodluckynews.domain.goodNews.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commentId")
    private Long id;

    @Column(name = "postId", nullable = false)
    private Long postId; // 댓글이 속한 게시글 ID

    @Column(name = "userId", nullable = false)
    private Long userId; // 댓글 작성자 ID

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 댓글 내용

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 댓글 작성 시간

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
