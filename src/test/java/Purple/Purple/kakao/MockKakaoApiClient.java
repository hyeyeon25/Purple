package Purple.Purple.kakao;

import Purple.Purple.kakao.dto.KakaoApiClient;
import Purple.Purple.kakao.dto.KakaoPlaceSearchResponse;

// 실제 API를 호출하지 않는 테스트 전용 가짜 클라이언트
public class MockKakaoApiClient implements KakaoApiClient {

    // 테스트 케이스마다 원하는 응답을 설정할 수 있도록 변수 선언
    private KakaoPlaceSearchResponse mockResponse;

    // 테스트 코드에서 이 메서드를 호출하여 가짜 응답을 미리 세팅
    public void setMockResponse(KakaoPlaceSearchResponse mockResponse) {
        this.mockResponse = mockResponse;
    }

    @Override
    public KakaoPlaceSearchResponse searchPlaces(String keyword, Double longitude, Double latitude, Integer radius, Integer page) {
        // 실제 API를 호출하는 대신, 미리 세팅된 mockResponse를 반환
        System.out.println("--- MockKakaoApiClient 동작: 실제 API를 호출하지 않습니다. ---");
        return this.mockResponse;
    }
}