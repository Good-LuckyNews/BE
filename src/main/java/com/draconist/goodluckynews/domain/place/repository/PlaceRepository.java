package com.draconist.goodluckynews.domain.place.repository;

import com.draconist.goodluckynews.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceRepository  extends JpaRepository<Place, Long> {
    Optional<Place> findByPlaceName(String placeName);//값이 있을 수도 있고 없을 수도
}

