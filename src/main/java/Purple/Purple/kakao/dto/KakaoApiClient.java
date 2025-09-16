package Purple.Purple.kakao.dto;


// 외부 API 호출을 담당하는 클라이언트 인터페이스
public interface KakaoApiClient {
    KakaoPlaceSearchResponse searchPlaces(String keyword, Double longitude, Double latitude, Integer radius, Integer page);

}