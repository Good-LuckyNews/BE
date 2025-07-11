package com.draconist.goodluckynews.domain.place.repository;

import com.draconist.goodluckynews.domain.place.entity.PlaceLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceLikeRepository extends JpaRepository<PlaceLike, Long> {

    int countByPlaceId(Long placeId);// 특정 플레이스의 좋아요 개수 조회

    boolean existsByPlaceIdAndUserId(Long placeId, Long userId);// 특정 사용자가 해당 플레이스를 좋아요 눌렀는지 확인

    Optional<PlaceLike> findByPlaceIdAndUserId(Long placeId, Long userId);
}
