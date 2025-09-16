package Purple.Purple.place.service;

import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import Purple.Purple.Neighborhood.repository.NeighborhoodRepository;
import Purple.Purple.kakao.dto.KakaoApiClient;
import Purple.Purple.kakao.dto.KakaoPlaceDocument;
import Purple.Purple.kakao.dto.KakaoPlaceSearchResponse;
import Purple.Purple.place.dto.PlaceCreateDto;
import Purple.Purple.place.dto.PlaceResponseDto;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.place.mapper.PlaceMapper;
import Purple.Purple.place.repository.PlaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;
    private final KakaoApiClient kakaoApiClient;
    private final NeighborhoodRepository neighborhoodRepository; // <<-- 1. 동네 리포지토리 주입

    @Override
    @Transactional
    public void fetchAllPlacesForCheonan() {
        // 1. 검색할 카테고리 키워드 목록 정의
        List<String> keywords = Arrays.asList("음식점", "카페", "문화시설", "관광명소", "공원", "쇼핑", "병원", "약국", "은행");

        // 2. <<-- 변경점: DB에서 모든 동네 정보 가져오기
        List<NeighborhoodEntity> neighborhoods = neighborhoodRepository.findAll();
        if (neighborhoods.isEmpty()) {
            log.warn("DB에 동네 정보가 없습니다. 데이터 구축을 진행할 수 없습니다.");
            return;
        }

        log.info("===== 천안시 전체 {}개 동네의 장소 데이터 저장을 시작합니다. =====", neighborhoods.size());

        // 3. DB에서 가져온 동네 목록을 순회하며 API 호출
        for (NeighborhoodEntity neighborhood : neighborhoods) {
            for (String keyword : keywords) {
                log.info(">> '{}' 지역 '{}' 카테고리 검색 시작", neighborhood.getNeighborhoodName(), keyword);
                int page = 1;
                while (true) {
                    try {
                        // 4. 페이지네이션을 통해 모든 결과 조회
                        KakaoPlaceSearchResponse response = kakaoApiClient.searchPlaces(
                                keyword,
                                neighborhood.getNeighborhoodLongitude(),
                                neighborhood.getNeighborhoodLatitude(),
                                2000, // 검색 반경 2km
                                page);

                        for (KakaoPlaceDocument doc : response.getDocuments()) {
                            // 5. 중복 체크 후 신규 장소만 저장
                            if (!placeRepository.findByKakaoPlaceId(doc.getId()).isPresent()) {
                                PlaceCreateDto createDto = placeMapper.toPlaceCreateDto(doc, neighborhood.getNeighborhoodId());
                                PlaceEntity newPlace = placeMapper.toEntity(createDto);
                                placeRepository.save(newPlace);
                                log.info("신규 장소 저장: {}", newPlace.getPlaceName());
                            }
                        }

                        if (response.getMeta().getIsEnd()) {
                            break; // 마지막 페이지면 중단
                        }
                        page++;

                        // 6. <<-- 개선점: API 서버에 부담을 주지 않기 위한 잠깐의 쉼
                        Thread.sleep(150); // 0.15초 대기

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("API 호출 대기 중 에러 발생", e);
                        break;
                    } catch (Exception e) {
                        log.error("'{}'-'{}' 검색 중 에러 발생 (페이지: {}): {}", neighborhood.getNeighborhoodName(), keyword, page, e.getMessage());
                        break; // 특정 검색에서 에러 발생 시 다음 키워드로 넘어감
                    }
                }
            }
        }
        log.info("===== 천안시 전체 장소 데이터 저장이 완료되었습니다. =====");
    }

    @Override
    @Transactional
    public PlaceResponseDto updatePlace(Integer placeId) {
        PlaceEntity place = placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 장소를 찾을 수 없습니다: " + placeId));

        KakaoPlaceSearchResponse searchResult = kakaoApiClient.searchPlaces(place.getPlaceName(), place.getLongitude(), place.getLatitude(), 100, 1);

        KakaoPlaceDocument latestData = searchResult.getDocuments().stream()
                .filter(doc -> doc.getId().equals(place.getKakaoPlaceId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("카카오 API에서 최신 정보를 찾지 못했습니다: " + place.getPlaceName()));

        place.setPlaceName(latestData.getPlaceName());
        place.setPlaceCategory(latestData.getCategoryName());
        place.setAddress(
                (latestData.getRoadAddressName() != null && !latestData.getRoadAddressName().isEmpty())
                        ? latestData.getRoadAddressName()
                        : latestData.getAddressName()
        );
        place.setLatitude(Double.parseDouble(latestData.getY()));
        place.setLongitude(Double.parseDouble(latestData.getX()));

        PlaceEntity updatedPlace = placeRepository.save(place);
        log.info("'{}' 장소 정보가 업데이트되었습니다.", updatedPlace.getPlaceName());

        return placeMapper.toResponseDto(updatedPlace);
    }
}