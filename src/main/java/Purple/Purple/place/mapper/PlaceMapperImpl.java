package Purple.Purple.place.mapper;

import Purple.Purple.kakao.dto.KakaoPlaceDocument;
import Purple.Purple.place.dto.PlaceCreateDto;
import Purple.Purple.place.dto.PlaceResponseDto;
import Purple.Purple.place.dto.RecommendedSlot;
import Purple.Purple.place.entity.PlaceEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;


@Component
public class PlaceMapperImpl implements PlaceMapper {
    //여행에서 추천하기 부적합한 요소들은 추천에서 제외
    private static final List<String> EXCLUSION_KEYWORDS = Arrays.asList(
            "장례식장", "빌딩", "주차장", "충전소", "주유", "LPG"
    );
    
    @Override
    public PlaceCreateDto toPlaceCreateDto(KakaoPlaceDocument document, Integer neighborhoodId) {
        if (document == null) {
            return null;
        }

        String categoryName = document.getCategoryName();
        if (isCategoryExcluded(categoryName)) {
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

        Boolean isIndoor = determineIsIndoor(document.getCategoryName());
        PlaceCreateDto createDto = new PlaceCreateDto();

        createDto.setKakaoPlaceId(document.getId());
        createDto.setPlaceName(document.getPlaceName());
        createDto.setPlaceCategory(document.getCategoryName());
        createDto.setAddress(address);
        createDto.setLatitude(latitude);
        createDto.setLongitude(longitude);
        createDto.setRecommendedSlot(recommendedSlot);
        createDto.setNeighborhoodId(neighborhoodId);

        if (isIndoor != null) {
            createDto.setIsIndoor(isIndoor);
        }

        return createDto;

    }

    // 여행 목적에 맞지 않는 카테고리의 장소가 추천되는 것을 방지하기 위한 함수
    private boolean isCategoryExcluded(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return false;
        }
        for (String keyword : EXCLUSION_KEYWORDS) {
            if (categoryName.contains(keyword)) {
                return true;
            }
        }
        return false;
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
                .summary(dto.getSummary())
                .isIndoor(dto.getIsIndoor())
                .stayDurationMinutes(dto.getStayDurationMinutes())
                .recommendedSlot(dto.getRecommendedSlot() != null ? dto.getRecommendedSlot().name() : null)
                .build();
    }

    @Override
    public PlaceResponseDto toResponseDto(PlaceEntity entity) {
        if (entity == null) {
            return null;
        }
        return PlaceResponseDto.from(entity);
    }

    // 카테고리 문자열 기반으로 추천 시간대를 결정함
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
        // 기본값으로 저녁 추천
        return RecommendedSlot.DINNER;
    }

    //가게의 실내/실외 구분
    private Boolean determineIsIndoor(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return null;
        }

        List<String> indoorKeywords = Arrays.asList(
                // 음식/음료
                "음식점", "카페", "술집", "뷔페", "분식", "치킨", "제과", "베이커리", "아이스크림", "다방",
                // 문화/예술/교육
                "도서관", "미술관", "문화원", "공연장", "연극극장", "전시관", "박물관", "기념관", "학습시설",
                // 쇼핑/생활
                "마트", "슈퍼마켓", "편의점", "꽃집", "세탁소", "부동산", "은행", "식품판매",
                "중고용품", "건강식품", "패션", "보석", "귀금속", "전자제품",
                // 실내 활동
                "클라이밍", "수영장", "실내동물원", "관광안내소"
        );

        List<String> outdoorKeywords = Arrays.asList(
                // 공원/자연
                "공원", "연못", "수목원", "식물원", "자연휴양림", "생태보존", "등산로", "놀이터",
                // 관광/명소
                "전망대", "하천", "테마거리", "광장", "동상", "도보여행", "고개", "저수지", "산", "호수",
                "계곡", "유원지", "천문대",
                // 문화유적
                "불상", "석불", "탑,비석", "릉,묘,총", "사당,제단", "산성,성곽", "유적지", "향교", "서당",
                // 스포츠 시설
                "농구장", "축구장", "테니스장", "풋살장", "운동장",
                // 교통/기타
                "공원시설물"
        );


        for (String keyword : indoorKeywords) {
            if (categoryName.contains(keyword)) {
                return true; // 실내 (1)
            }
        }

        for (String keyword : outdoorKeywords) {
            if (categoryName.contains(keyword)) {
                return false; // 야외 (0)
            }
        }

        // 키워드에 해당하지 않으면 기본값을 사용하도록 null 반환
        return null;
    }
}
