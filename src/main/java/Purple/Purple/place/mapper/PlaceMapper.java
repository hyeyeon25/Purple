package Purple.Purple.place.mapper;

import Purple.Purple.kakao.dto.KakaoPlaceDocument;
import Purple.Purple.place.dto.PlaceCreateDto;
import Purple.Purple.place.dto.PlaceResponseDto;
import Purple.Purple.place.entity.PlaceEntity;

/**
 * - DTO와 Entity 간의 데이터 변환 로직 정의
 * - 구현체(PlaceMapperImpl)에서 실제 변환 로직을 작성
 */
public interface PlaceMapper {

    PlaceCreateDto toPlaceCreateDto(KakaoPlaceDocument document, Integer neighborhoodId);
    PlaceEntity toEntity(PlaceCreateDto dto);
    PlaceResponseDto toResponseDto(PlaceEntity entity);

}