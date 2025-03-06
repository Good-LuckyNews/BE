package com.draconist.goodluckynews.domain.goodNews.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CommentLike")
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commentLikeId")
    private Long id;

    @Column(name = "userId", nullable = false)
    private Long userId; // 좋아요 누른 사용자 ID

    @Column(name = "commentId", nullable = false)
    private Long commentId; // 좋아요가 눌린 댓글 ID
}
