package com.draconist.goodluckynews.domain.article.entity;

import com.draconist.goodluckynews.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SevenCompletedGraph {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sevenCompletedGraphId")
    private Long id;

    @Column(name = "first")
    private Integer first;
    @Column(name = "second")
    private Integer second;
    @Column(name = "third")
    private Integer third;
    @Column(name = "fourth")
    private Integer fourth;
    @Column(name = "fifth")
    private Integer fifth;
    @Column(name = "sixth")
    private Integer sixth;
    @Column(name = "seventh")
    private Integer seventh;

}
