package Purple.Purple.place.service;

import Purple.Purple.place.dto.PlaceResponseDto;

import java.util.List;

// Service 인터페이스
// - 장소 데이터 처리와 관련된 핵심 비즈니스 로직의 명세를 정의합니다.
//- Controller는 이 인터페이스에 의존합니다.

public interface PlaceService {

    void fetchAllPlacesForCheonan();
    PlaceResponseDto updatePlace(Integer placeId);
}