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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;
    private final KakaoApiClient kakaoApiClient;
    private final NeighborhoodRepository neighborhoodRepository;

    @Override
    @Transactional
    public void fetchAllPlacesForCheonan() {
        // API가 호출되었을 때, 동네 데이터가 없으면 즉시 생성합니다.
        if (neighborhoodRepository.count() == 0) {
            initializeNeighborhoods();
        }

        // 검색할 카테고리 키워드 목록 정의
        List<String> keywords = Arrays.asList("음식점", "카페", "문화시설", "관광명소", "공원", "쇼핑");

        // DB에서 모든 동네 정보 가져오기
        List<NeighborhoodEntity> neighborhoods = neighborhoodRepository.findAll();

        log.info("===== 천안시 전체 {}개 동네의 장소 데이터 저장을 시작합니다. =====", neighborhoods.size());

        for (NeighborhoodEntity neighborhood : neighborhoods) {
            for (String keyword : keywords) {
                // [수정됨!] 각 작업의 시작을 명확하게 로그로 남깁니다.
                log.info("- '{}' 지역 '{}' 카테고리 데이터 수집을 시작합니다.", neighborhood.getNeighborhoodName(), keyword);
                boolean success = true; // 작업 성공 여부를 추적하는 플래그
                int page = 1;
                int savedCount = 0; // 이번 작업으로 새로 저장된 장소의 수

                while (true) {
                    try {
                        KakaoPlaceSearchResponse response = kakaoApiClient.searchPlaces(
                                keyword,
                                neighborhood.getNeighborhoodLongitude(),
                                neighborhood.getNeighborhoodLatitude(),
                                2000,
                                page);

                        if (response != null && response.getDocuments() != null) {
                            for (KakaoPlaceDocument doc : response.getDocuments()) {
                                if (placeRepository.findByKakaoPlaceId(doc.getId()).isEmpty()) {
                                    PlaceCreateDto createDto = placeMapper.toPlaceCreateDto(doc, neighborhood.getNeighborhoodId());
                                    if (createDto != null) {
                                        PlaceEntity newPlace = placeMapper.toEntity(createDto);
                                        if (newPlace != null) {
                                            newPlace.setNeighborhood(neighborhood);
                                            placeRepository.save(newPlace);
                                            savedCount++; // 저장 카운트 증가
                                        }
                                    }
                                }
                            }
                        }

                        if (response == null || response.getMeta() == null || response.getMeta().getIsEnd()) {
                            break;
                        }
                        page++;

                        Thread.sleep(150);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("- '{}' 지역 '{}' 카테고리 수집 실패: {}", neighborhood.getNeighborhoodName(), keyword, "API 호출 대기 중 에러 발생");
                        success = false;
                        break;
                    } catch (Exception e) {
                        log.error("- '{}' 지역 '{}' 카테고리 수집 실패: {}", neighborhood.getNeighborhoodName(), keyword, e.getMessage());
                        success = false;
                        break;
                    }
                }
                if (success) {
                    log.info("- '{}' 지역 '{}' 카테고리 수집 완료 (신규 저장: {}건)", neighborhood.getNeighborhoodName(), keyword, savedCount);
                }
            }
        }
        log.info("===== 천안시 전체 장소 데이터 저장이 완료되었습니다. =====");
    }

    private void initializeNeighborhoods() {
        log.info("Neighborhood 테이블이 비어있습니다. 천안시 동네 데이터를 초기화합니다.");
        List<NeighborhoodEntity> cheonanNeighborhoods = Arrays.asList(
                NeighborhoodEntity.builder().neighborhoodName("신부동").neighborhoodLatitude(36.8184).neighborhoodLongitude(127.1528).build(),
                NeighborhoodEntity.builder().neighborhoodName("안서동").neighborhoodLatitude(36.8333).neighborhoodLongitude(127.1793).build(),
                NeighborhoodEntity.builder().neighborhoodName("봉명동").neighborhoodLatitude(36.8078).neighborhoodLongitude(127.1354).build(),
                NeighborhoodEntity.builder().neighborhoodName("일봉동").neighborhoodLatitude(36.7972).neighborhoodLongitude(127.1396).build(),
                NeighborhoodEntity.builder().neighborhoodName("신방동").neighborhoodLatitude(36.7858).neighborhoodLongitude(127.1283).build(),
                NeighborhoodEntity.builder().neighborhoodName("청룡동").neighborhoodLatitude(36.7819).neighborhoodLongitude(127.1558).build(),
                NeighborhoodEntity.builder().neighborhoodName("원성동").neighborhoodLatitude(36.8105).neighborhoodLongitude(127.1568).build(),
                NeighborhoodEntity.builder().neighborhoodName("문성동").neighborhoodLatitude(36.8122).neighborhoodLongitude(127.1465).build(),
                NeighborhoodEntity.builder().neighborhoodName("중앙동").neighborhoodLatitude(36.8093).neighborhoodLongitude(127.1461).build(),
                NeighborhoodEntity.builder().neighborhoodName("목천읍").neighborhoodLatitude(36.7725).neighborhoodLongitude(127.2342).build(),
                NeighborhoodEntity.builder().neighborhoodName("풍세면").neighborhoodLatitude(36.7261).neighborhoodLongitude(127.1008).build(),
                NeighborhoodEntity.builder().neighborhoodName("광덕면").neighborhoodLatitude(36.6917).neighborhoodLongitude(127.1697).build(),
                NeighborhoodEntity.builder().neighborhoodName("북면").neighborhoodLatitude(36.8778).neighborhoodLongitude(127.2889).build(),
                NeighborhoodEntity.builder().neighborhoodName("성남면").neighborhoodLatitude(36.8167).neighborhoodLongitude(127.2428).build(),
                NeighborhoodEntity.builder().neighborhoodName("수신면").neighborhoodLatitude(36.8525).neighborhoodLongitude(127.3200).build(),
                NeighborhoodEntity.builder().neighborhoodName("병천면").neighborhoodLatitude(36.8833).neighborhoodLongitude(127.2833).build(),
                NeighborhoodEntity.builder().neighborhoodName("동면").neighborhoodLatitude(36.8525).neighborhoodLongitude(127.2589).build(),
                NeighborhoodEntity.builder().neighborhoodName("성정동").neighborhoodLatitude(36.8189).neighborhoodLongitude(127.1328).build(),
                NeighborhoodEntity.builder().neighborhoodName("쌍용동").neighborhoodLatitude(36.7947).neighborhoodLongitude(127.1175).build(),
                NeighborhoodEntity.builder().neighborhoodName("불당동").neighborhoodLatitude(36.8151).neighborhoodLongitude(127.1139).build(),
                NeighborhoodEntity.builder().neighborhoodName("두정동").neighborhoodLatitude(36.8339).neighborhoodLongitude(127.1428).build(),
                NeighborhoodEntity.builder().neighborhoodName("부성동").neighborhoodLatitude(36.8481).neighborhoodLongitude(127.1225).build(),
                NeighborhoodEntity.builder().neighborhoodName("백석동").neighborhoodLatitude(36.8228).neighborhoodLongitude(127.1189).build(),
                NeighborhoodEntity.builder().neighborhoodName("성환읍").neighborhoodLatitude(36.9189).neighborhoodLongitude(127.1264).build(),
                NeighborhoodEntity.builder().neighborhoodName("성거읍").neighborhoodLatitude(36.8844).neighborhoodLongitude(127.1583).build(),
                NeighborhoodEntity.builder().neighborhoodName("직산읍").neighborhoodLatitude(36.8789).neighborhoodLongitude(127.1158).build(),
                NeighborhoodEntity.builder().neighborhoodName("입장면").neighborhoodLatitude(36.9119).neighborhoodLongitude(127.2514).build()
        );
        neighborhoodRepository.saveAll(cheonanNeighborhoods);
        log.info("총 {}개의 천안시 동네 데이터 초기화를 완료했습니다.", cheonanNeighborhoods.size());
    }

    @Override
    @Transactional
    public PlaceResponseDto updatePlace(Integer placeId) {
        PlaceEntity place = placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 장소를 찾을 수 없습니다: " + placeId));

        KakaoPlaceSearchResponse searchResult = kakaoApiClient.searchPlaces(place.getPlaceName(), place.getLongitude(), place.getLatitude(), 100, 1);

        if (searchResult == null || searchResult.getDocuments() == null || searchResult.getDocuments().isEmpty()) {
            throw new RuntimeException("카카오 API에서 최신 정보를 찾지 못했습니다: " + place.getPlaceName());
        }

        KakaoPlaceDocument latestData = searchResult.getDocuments().stream()
                .filter(doc -> doc.getId().equals(place.getKakaoPlaceId()))
                .findFirst()
                .orElse(searchResult.getDocuments().get(0));

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

