package Purple.Purple.place.mapper;

import Purple.Purple.kakao.dto.KakaoPlaceDocument;
import Purple.Purple.place.dto.PlaceCreateDto;
import Purple.Purple.place.dto.PlaceResponseDto;
import Purple.Purple.place.entity.PlaceEntity;


public interface PlaceMapper {

    PlaceCreateDto toPlaceCreateDto(KakaoPlaceDocument document, Integer neighborhoodId);
    PlaceEntity toEntity(PlaceCreateDto dto);
    PlaceResponseDto toResponseDto(PlaceEntity entity);

}