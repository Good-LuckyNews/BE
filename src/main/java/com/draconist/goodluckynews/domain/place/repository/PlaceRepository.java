package com.draconist.goodluckynews.domain.place.repository;

import com.draconist.goodluckynews.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PlaceRepository  extends JpaRepository<Place, Long> {
    Optional<Place> findByPlaceName(String placeName);//값이 있을 수도 있고 없을 수도
    Page<Place> findAll(Pageable pageable);// 페이지네이션 지원하는 조회 메서드 추가
    Optional<Place> findById(Long placeId); // 특정 placeId로 플레이스를 찾는 메서드 추가

}

