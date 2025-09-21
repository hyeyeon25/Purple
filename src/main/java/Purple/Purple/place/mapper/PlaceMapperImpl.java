package Purple.Purple.place.mapper;

import Purple.Purple.kakao.dto.KakaoPlaceDocument;
import Purple.Purple.place.dto.PlaceCreateDto;
import Purple.Purple.place.dto.PlaceResponseDto;
import Purple.Purple.place.dto.RecommendedSlot;
import Purple.Purple.place.entity.PlaceEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Mapper 구현체
 *
 * - @Component 어노테이션을 통해 Spring Bean으로 등록하여 Service 계층에서 주입받아 사용합니다.
 * - 설계 문서에 명시된 비즈니스 규칙을 적용하여 데이터를 변환합니다.
 */
@Component // Spring의 Service나 다른 컴포넌트에서 주입(DI)받을 수 있도록 Bean으로 등록
public class PlaceMapperImpl implements PlaceMapper {

    @Override
    public PlaceCreateDto toPlaceCreateDto(KakaoPlaceDocument document, Integer neighborhoodId) {
        if (document == null) {
            return null;
        }

        // 1. 주소 결정 로직: 도로명 주소를 우선 사용
        String address = StringUtils.hasText(document.getRoadAddressName())
                ? document.getRoadAddressName()
                : document.getAddressName();

        // 2. 좌표 파싱
        Double longitude = Double.parseDouble(document.getX());
        Double latitude = Double.parseDouble(document.getY());

        // 3. 추천 시간대 추론
        RecommendedSlot recommendedSlot = determineRecommendedSlot(document.getCategoryName());

        return PlaceCreateDto.builder()
                .kakaoPlaceId(document.getId())
                .placeName(document.getPlaceName())
                .placeCategory(document.getCategoryName())
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .recommendedSlot(recommendedSlot)
                .neighborhoodId(neighborhoodId)
                // --- 아래는 Google API로 채워질 필드 ---
                // isIndoor, stayDurationMinutes 등은 DTO의 @Builder.Default로 기본값 설정됨
                // openTime, closeTime 등은 null로 초기화 (이후 Google API로 보강)
                .build();
    }

    @Override
    public PlaceEntity toEntity(PlaceCreateDto dto) {
        if (dto == null) {
            return null;
        }

        return PlaceEntity.builder()
                .kakaoPlaceId(dto.getKakaoPlaceId())
                .placeName(dto.getPlaceName())
                .placeCategory(dto.getPlaceCategory())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .scoreFood(dto.getScoreFood())
                .scoreCafe(dto.getScoreCafe())
                .scoreActivity(dto.getScoreActivity())
                .scoreCulture(dto.getScoreCulture())
                .summary(dto.getSummary())
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .breakStartTime(dto.getBreakStartTime())
                .breakEndTime(dto.getBreakEndTime())
                .isIndoor(dto.getIsIndoor())
                .stayDurationMinutes(dto.getStayDurationMinutes())
                .recommendedSlot(dto.getRecommendedSlot() != null ? dto.getRecommendedSlot().name() : null)
                // neighborhood는 Service 계층에서 neighborhoodId를 이용해 조회 후 직접 설정해야 함
                .build();
    }

    @Override
    public PlaceResponseDto toResponseDto(PlaceEntity entity) {
        if (entity == null) {
            return null;
        }
        // 정적 팩토리 메서드를 사용하여 변환
        return PlaceResponseDto.from(entity);
    }

    /**
     * 카테고리 문자열을 기반으로 추천 시간대를 결정하는 private 헬퍼 메서드.
     * 실제 서비스에서는 더 정교한 규칙이 필요할 수 있습니다.
     *
     * @param categoryName 카카오 장소 카테고리
     * @return 추천 시간대 Enum
     */
    private RecommendedSlot determineRecommendedSlot(String categoryName) {
        if (categoryName == null) {
            return null;
        }
        if (categoryName.contains("카페")) {
            return RecommendedSlot.AFTERNOON;
        }
        if (categoryName.contains("아침") || categoryName.contains("브런치")) {
            return RecommendedSlot.BREAKFAST;
        }
        if (categoryName.contains("점심")) {
            return RecommendedSlot.LUNCH;
        }
        if (categoryName.contains("술집") || categoryName.contains("바")) {
            return RecommendedSlot.NIGHT;
        }
        // 기본값으로 저녁을 추천 (음식점 등)
        return RecommendedSlot.DINNER;
    }
}
