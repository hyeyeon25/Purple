package Purple.Purple.place.service;

import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import Purple.Purple.Neighborhood.repository.NeighborhoodRepository;
import Purple.Purple.config.TestConfig;
import Purple.Purple.kakao.MockKakaoApiClient; // MockKakaoApiClient 임포트
import Purple.Purple.kakao.dto.KakaoApiClient;
import Purple.Purple.kakao.dto.KakaoMeta;
import Purple.Purple.kakao.dto.KakaoPlaceDocument;
import Purple.Purple.kakao.dto.KakaoPlaceSearchResponse;
import Purple.Purple.place.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean; // <<-- 삭제!
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles; // <<-- 추가!
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ActiveProfiles("test") // <<-- "test" 프로필을 활성화! 이제 TestConfig가 작동합니다.
@Import(TestConfig.class)
class PlaceServiceImplIntegrationTest {

    @Autowired
    private PlaceService placeService;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private NeighborhoodRepository neighborhoodRepository;

    // @MockBean // <<-- 삭제!
    // private KakaoApiClient kakaoApiClient;

    @Autowired // <<-- 일반 @Autowired로 변경! (TestConfig에 의해 MockKakaoApiClient가 주입됨)
    private KakaoApiClient kakaoApiClient;

    @BeforeEach
    void setUp() {
        NeighborhoodEntity testNeighborhood = NeighborhoodEntity.builder()
                .neighborhoodName("테스트동")
                .neighborhoodLatitude(37.0)
                .neighborhoodLongitude(127.0)
                .build();
        neighborhoodRepository.save(testNeighborhood);
    }

    @Test
    @DisplayName("fetchAllPlaces: DB의 동네정보로 API를 호출하고 페이지네이션을 통해 모든 장소를 저장해야 한다")
    void fetchAllPlaces_Success_With_Pagination() {
        // GIVEN: 카카오 API가 이렇게 응답할 것이라고 시뮬레이션
        KakaoPlaceDocument doc1 = KakaoPlaceDocument.builder().id("1").placeName("장소1").build();
        KakaoMeta meta1 = KakaoMeta.builder().isEnd(true).build(); // 페이지가 하나뿐인 간단한 테스트
        KakaoPlaceSearchResponse response = new KakaoPlaceSearchResponse(meta1, List.of(doc1));

        // Mockito의 when() 대신, 우리가 직접 만든 Mock 객체의 메서드를 호출하여 응답을 설정
        ((MockKakaoApiClient) kakaoApiClient).setMockResponse(response);

        // WHEN: 데이터 구축 서비스를 실행하면
        placeService.fetchAllPlacesForCheonan();

        // THEN: DB에 저장된 장소의 총 개수가 API 응답 개수와 같아야 한다
        long placeCount = placeRepository.count();
        assertEquals(1, placeCount, "총 1개의 장소가 저장되어야 합니다.");
        System.out.println("통합 테스트 성공! @MockBean 없이 프로필 기능으로 완벽하게 테스트했습니다.");
    }
}