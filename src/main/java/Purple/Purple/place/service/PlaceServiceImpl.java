package Purple.Purple.place.service;

import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import Purple.Purple.place.entity.PlaceAnalysisEntity;
import Purple.Purple.Neighborhood.repository.NeighborhoodRepository;
import Purple.Purple.place.repository.PlaceAnalysisRepository;
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
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;
    private final KakaoApiClient kakaoApiClient;
    private final NeighborhoodRepository neighborhoodRepository;
    private final PlaceAnalysisRepository placeAnalysisRepository;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public void fetchAllPlacesForCheonan() {
        // 배치 시작 시간 기록
        LocalDateTime startTime = LocalDateTime.now();
        log.info("===== 배치 시작 시간: {} =====", startTime);

        // 1. 동네 데이터가 없으면 초기화
        if (neighborhoodRepository.count() == 0) {
            initializeNeighborhoods();
        }

        List<String> keywords = Arrays.asList("음식점", "카페", "문화시설", "관광명소", "공원", "쇼핑");

        List<NeighborhoodEntity> neighborhoods = neighborhoodRepository.findAll();
        Map<String, NeighborhoodEntity> neighborhoodMap = neighborhoods.stream()
                .collect(Collectors.toMap(NeighborhoodEntity::getNeighborhoodName, Function.identity()));

        log.info("===== 천안시 전체 {}개 동네의 장소 데이터 저장을 시작합니다. =====", neighborhoods.size());

        // 3. 각 거점(좌표)을 순회하며 데이터 수집
        for (NeighborhoodEntity neighborhood : neighborhoods) {

            boolean success = true; // 작업 성공 여부
            int savedCount = 0; // 이번 작업으로 새로 저장된 장소의 수
            int updatedCount = 0; // 이번 작업으로 업데이트된 장소의 수

            for (String keyword : keywords) {
                int page = 1;

                while (true) {
                    try {
                        KakaoPlaceSearchResponse response = kakaoApiClient.searchPlaces(
                                keyword,
                                neighborhood.getNeighborhoodLongitude(),
                                neighborhood.getNeighborhoodLatitude(),
                                2000,
                                page);

                        if (response == null || response.getDocuments() == null) {
                            break;
                        }

                        for (KakaoPlaceDocument doc : response.getDocuments()) {
                            updatedCount++;

                            // API 결과의 지번 주소를 파싱하여 우리가 관리하는 동네인지 확인
                            String detectedNeighborhoodName = extractNeighborhoodFromAddress(doc.getAddressName());

                            // 우리가 관리하는 동네 리스트에 없는 지역이면 저장 X
                            if (detectedNeighborhoodName == null || !neighborhoodMap.containsKey(detectedNeighborhoodName)) {
                                continue;
                            }

                            NeighborhoodEntity targetNeighborhood = neighborhoodMap.get(detectedNeighborhoodName);

                            // 저장 로직
                            var existingPlace = placeRepository.findByKakaoPlaceId(doc.getId());
                            if (existingPlace.isEmpty()) {
                                PlaceCreateDto createDto = placeMapper.toPlaceCreateDto(doc, targetNeighborhood.getNeighborhoodId());
                                if (createDto != null) {
                                    PlaceEntity newPlace = placeMapper.toEntity(createDto);
                                    if (newPlace != null) {
                                        newPlace.setNeighborhood(targetNeighborhood);
                                        newPlace.setLastSyncedAt(startTime);
                                        newPlace.setIsClosed(false);
                                        placeRepository.save(newPlace);
                                        savedCount++;
                                    }
                                }
                            } else {
                                // 기존 장소 업데이트
                                PlaceEntity place = existingPlace.get();
                                // 혹시 기존에 잘못된 동네로 분류되어 있었다면 수정
                                if (!place.getNeighborhood().getNeighborhoodId().equals(targetNeighborhood.getNeighborhoodId())) {
                                    place.setNeighborhood(targetNeighborhood);
                                }
                                place.setLastSyncedAt(startTime);
                                place.setIsClosed(false);
                                placeRepository.save(place);
                            }
                        }

                        if (response.getMeta() == null || response.getMeta().getIsEnd()) {
                            break;
                        }
                        page++;
                        Thread.sleep(100); // API 부하 조절

                    } catch (Exception e) {
                        log.error("- '{}' 지역 '{}' 카테고리 수집 실패: {}", neighborhood.getNeighborhoodName(), keyword,
                                e.getMessage());
                        success = false;
                        break;
                    }
                }
            }
            // 일주일 동안 단 한 번도 카카오 API 검색 결과에 안 떴을 때만 진짜 폐업으로 간주
            LocalDateTime thresholdTime = startTime.minusDays(7);
            int closedCount = placeRepository.markClosedPlacesByNeighborhood(neighborhood, thresholdTime);
            if (closedCount > 0) {
                log.info("- '{}' 지역에서 {}건의 장소가 폐업 처리되었습니다.", neighborhood.getNeighborhoodName(), closedCount);
            }

            if (success) {
                log.info("- '{}' 지역 데이터 수집 완료 (기존 업데이트 {}건, 신규 저장: {}건, 폐업 처리: {}건)",
                        neighborhood.getNeighborhoodName(), updatedCount, savedCount, closedCount);
            }
        }
    }

    /**
     * 주소 문자열에서 '동/읍/면' 이름을 추출하는 헬퍼 메서드
     * 예: "충남 천안시 동남구 신부동 123" -> "신부동"
     */
    private String extractNeighborhoodFromAddress(String addressName) {
        if (addressName == null || addressName.isEmpty()) {
            return null;
        }

        // 공백으로 분리
        String[] tokens = addressName.split(" ");
        for (String token : tokens) {
            // '동', '읍', '면'으로 끝나는 단어 찾기
            if (token.endsWith("동") || token.endsWith("읍") || token.endsWith("면")) {
                return token;
            }
        }
        return null;
    }

    private void initializeNeighborhoods() {
        log.info("Neighborhood 테이블이 비어있습니다. 천안시 동네 데이터를 초기화합니다.");
        List<NeighborhoodEntity> cheonanNeighborhoods = Arrays.asList(
                NeighborhoodEntity.builder().neighborhoodName("신부동").neighborhoodLatitude(36.8184).neighborhoodLongitude(127.1528).build(),
                NeighborhoodEntity.builder().neighborhoodName("안서동").neighborhoodLatitude(36.8333).neighborhoodLongitude(127.1793).build(),
                NeighborhoodEntity.builder().neighborhoodName("원성동").neighborhoodLatitude(36.8105).neighborhoodLongitude(127.1568).build(),
                NeighborhoodEntity.builder().neighborhoodName("유량동").neighborhoodLatitude(36.8230).neighborhoodLongitude(127.1680).build(),
                NeighborhoodEntity.builder().neighborhoodName("대흥동").neighborhoodLatitude(36.8100).neighborhoodLongitude(127.1460).build(),
                NeighborhoodEntity.builder().neighborhoodName("문화동").neighborhoodLatitude(36.8120).neighborhoodLongitude(127.1480).build(),
                NeighborhoodEntity.builder().neighborhoodName("성황동").neighborhoodLatitude(36.8160).neighborhoodLongitude(127.1490).build(),
                NeighborhoodEntity.builder().neighborhoodName("오룡동").neighborhoodLatitude(36.8080).neighborhoodLongitude(127.1500).build(),
                NeighborhoodEntity.builder().neighborhoodName("사직동").neighborhoodLatitude(36.8050).neighborhoodLongitude(127.1480).build(),
                NeighborhoodEntity.builder().neighborhoodName("영성동").neighborhoodLatitude(36.8070).neighborhoodLongitude(127.1520).build(),
                NeighborhoodEntity.builder().neighborhoodName("청당동").neighborhoodLatitude(36.7870).neighborhoodLongitude(127.1520).build(),
                NeighborhoodEntity.builder().neighborhoodName("청수동").neighborhoodLatitude(36.7920).neighborhoodLongitude(127.1450).build(),
                NeighborhoodEntity.builder().neighborhoodName("삼룡동").neighborhoodLatitude(36.7820).neighborhoodLongitude(127.1650).build(),
                NeighborhoodEntity.builder().neighborhoodName("구성동").neighborhoodLatitude(36.8020).neighborhoodLongitude(127.1600).build(),
                NeighborhoodEntity.builder().neighborhoodName("구룡동").neighborhoodLatitude(36.7720).neighborhoodLongitude(127.1430).build(),
                NeighborhoodEntity.builder().neighborhoodName("봉명동").neighborhoodLatitude(36.8078).neighborhoodLongitude(127.1354).build(),
                NeighborhoodEntity.builder().neighborhoodName("다가동").neighborhoodLatitude(36.8020).neighborhoodLongitude(127.1380).build(),
                NeighborhoodEntity.builder().neighborhoodName("용곡동").neighborhoodLatitude(36.7880).neighborhoodLongitude(127.1350).build(),
                NeighborhoodEntity.builder().neighborhoodName("신방동").neighborhoodLatitude(36.7858).neighborhoodLongitude(127.1283).build(),
                NeighborhoodEntity.builder().neighborhoodName("불당동").neighborhoodLatitude(36.8151).neighborhoodLongitude(127.1139).build(),
                NeighborhoodEntity.builder().neighborhoodName("백석동").neighborhoodLatitude(36.8228).neighborhoodLongitude(127.1189).build(),
                NeighborhoodEntity.builder().neighborhoodName("두정동").neighborhoodLatitude(36.8339).neighborhoodLongitude(127.1428).build(),
                NeighborhoodEntity.builder().neighborhoodName("성정동").neighborhoodLatitude(36.8189).neighborhoodLongitude(127.1328).build(),
                NeighborhoodEntity.builder().neighborhoodName("쌍용동").neighborhoodLatitude(36.7947).neighborhoodLongitude(127.1175).build(),
                NeighborhoodEntity.builder().neighborhoodName("부대동").neighborhoodLatitude(36.8550).neighborhoodLongitude(127.1300).build(),
                NeighborhoodEntity.builder().neighborhoodName("신당동").neighborhoodLatitude(36.8600).neighborhoodLongitude(127.1200).build(),
                NeighborhoodEntity.builder().neighborhoodName("업성동").neighborhoodLatitude(36.8500).neighborhoodLongitude(127.1150).build(),
                NeighborhoodEntity.builder().neighborhoodName("차암동").neighborhoodLatitude(36.8370).neighborhoodLongitude(127.1020).build(),
                NeighborhoodEntity.builder().neighborhoodName("목천읍").neighborhoodLatitude(36.7725).neighborhoodLongitude(127.2342).build(),
                NeighborhoodEntity.builder().neighborhoodName("풍세면").neighborhoodLatitude(36.7261).neighborhoodLongitude(127.1008).build(),
                NeighborhoodEntity.builder().neighborhoodName("광덕면").neighborhoodLatitude(36.6917).neighborhoodLongitude(127.1697).build(),
                NeighborhoodEntity.builder().neighborhoodName("북면").neighborhoodLatitude(36.8778).neighborhoodLongitude(127.2889).build(),
                NeighborhoodEntity.builder().neighborhoodName("성남면").neighborhoodLatitude(36.8167).neighborhoodLongitude(127.2428).build(),
                NeighborhoodEntity.builder().neighborhoodName("수신면").neighborhoodLatitude(36.8525).neighborhoodLongitude(127.3200).build(),
                NeighborhoodEntity.builder().neighborhoodName("병천면").neighborhoodLatitude(36.8833).neighborhoodLongitude(127.2833).build(),
                NeighborhoodEntity.builder().neighborhoodName("동면").neighborhoodLatitude(36.8525).neighborhoodLongitude(127.2589).build(),
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

        KakaoPlaceSearchResponse searchResult = kakaoApiClient.searchPlaces(place.getPlaceName(), place.getLongitude(),
                place.getLatitude(), 100, 1);

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
                        : latestData.getAddressName());
        place.setLatitude(Double.parseDouble(latestData.getY()));
        place.setLongitude(Double.parseDouble(latestData.getX()));

        PlaceEntity updatedPlace = placeRepository.save(place);
        log.info("'{}' 장소 정보가 업데이트되었습니다.", updatedPlace.getPlaceName());

        return placeMapper.toResponseDto(updatedPlace);
    }

    /**
     * 특정 장소의 분석 데이터를 조회
     * DB에 분석 결과가 없으면 파이썬 분석 서버를 호출하여 데이터를 생성하고 동기화
     * * @param dbPlaceId 장소 고유 ID
     *
     * @param placeName 장소 이름
     * @return PlaceAnalysisEntity 분석 결과 엔티티 (실패 시 null)
     */

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PlaceAnalysisEntity getAnalysisData(Integer dbPlaceId, String placeName) {

        // 장소 조회
        PlaceEntity place = placeRepository.findById(dbPlaceId)
                .orElseThrow(() -> new EntityNotFoundException("장소를 찾을 수 없습니다 ID: " + dbPlaceId));

        // 이미 분석된 데이터가 있는지 확인
        if (place.getPlaceAnalysis() != null) {

            // 데이터 있는데 summary 비워져 있을시 저장
            if (place.getSummary() == null || place.getSummary().isEmpty()) {
                log.info("🔧 기존 분석 데이터는 있지만 요약이 비어있어 동기화합니다. (Place ID: {})", dbPlaceId);

                // 만약 저장 안될때 강제 저장
                placeRepository.updatePlaceSummary(dbPlaceId, place.getPlaceAnalysis().getReviewSummary());
            }

            return place.getPlaceAnalysis();
        }

        // 분석 데이터 없을 시 파이썬 호출
        String address = place.getAddress();
        log.info("파이썬에게 분석 요청 (URL: {}): 이름='{}', 주소='{}'", aiServerUrl, placeName, address);

        try {
            // 한글 깨짐 방지 UriComponentsBuilder 사용
            URI uri = UriComponentsBuilder
                    .fromUriString(aiServerUrl)
                    .path("/analyze")
                    .queryParam("db_place_id", dbPlaceId)
                    .queryParam("place_name", placeName)
                    .queryParam("address", address)
                    .encode()
                    .build()
                    .toUri();

            restTemplate.getForEntity(uri, String.class);

            // 파이썬 저장 및 DB다시 조회
            PlaceAnalysisEntity analysis = placeAnalysisRepository.findByPlace_PlaceId(dbPlaceId)
                    .orElse(null);

            // 새로 받아온 데이터 저장
            if (analysis != null) {
                // 저장 안될시 강제 저장
                placeRepository.updatePlaceSummary(dbPlaceId, analysis.getReviewSummary());
                log.info("Place 테이블 summary 강제 업데이트 완료");
            }

            return analysis;

        } catch (Exception e) {
            log.error("파이썬 호출 실패: {}", e.getMessage());
            return null;
        }
    }
}