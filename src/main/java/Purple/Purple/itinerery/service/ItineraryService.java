package Purple.Purple.itinerery.service;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.folder.domain.FolderPlace;
import Purple.Purple.folder.dto.FolderPlaceResponseDto;
import Purple.Purple.folder.exception.InvalidFolderRequestException;
import Purple.Purple.folder.repository.FolderPlaceRepository;
import Purple.Purple.folder.service.FolderValidator;
import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.itinerery.domain.ItineraryPlace;
import Purple.Purple.itinerery.repository.ItineraryPlaceRepository;
import Purple.Purple.itinerery.repository.ItineraryRepository;
import Purple.Purple.tmap.dto.TmapFeature;
import Purple.Purple.tmap.dto.TmapPedestrianResponse;
import Purple.Purple.tmap.dto.TmapWalkingApiClient;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// 여행 경로 관리 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryService {

	private final FolderPlaceRepository folderPlaceRepository;
	private final ItineraryRepository itineraryRepository;
	private final ItineraryPlaceRepository itineraryPlaceRepository;
	private final PlaceRepository placeRepository;
	private final EntityManager entityManager;
	private final TmapWalkingApiClient tmapWalkingApiClient;
	private final RouteOptimizationService routeOptimizationService;
	private final FolderValidator folderValidator;

	// 폴더의 경로 조회 (장소 ID 리스트)
	@Transactional(readOnly = true)
	public List<Integer> getRoute(Integer folderId, Long userId) {
		log.info("경로 조회 시작 - folderId: {}, userId: {}", folderId, userId);

		Folder folder = folderValidator.findAndValidateFolderByUserId(folderId, userId);
		Optional<Itinerary> optionalItinerary = itineraryRepository.findByFolder(folder);

		if (optionalItinerary.isPresent()) {
			Itinerary itinerary = optionalItinerary.get();
			List<ItineraryPlace> itineraryPlaces = loadItineraryPlacesWithPlace(itinerary);

			// 경로가 있고 방문 순서가 설정되어 있으면 반환
			if (hasValidOrder(itineraryPlaces)) {
				List<Integer> route = itineraryPlaces.stream()
						.sorted(Comparator.comparing(ip -> ip.getVisitOrder() == null ? 0 : ip.getVisitOrder()))
						.map(ip -> ip.getPlace().getPlaceId())
						.collect(Collectors.toList());

				log.info("경로 조회 완료 - folderId: {}, placeCount: {}", folderId, route.size());
				return route;
			}
		}

		// 경로가 없거나 비어있으면 빈 리스트 반환
		log.info("경로 없음 - folderId: {}", folderId);
		return List.of();
	}

	// 폴더의 경로 상세 조회 (도보 경로 정보 포함)
	@Transactional(readOnly = true)
	public List<FolderPlaceResponseDto> getRouteDetails(Integer folderId, Long userId) {
		log.info("경로 상세 조회 시작 - folderId: {}, userId: {}", folderId, userId);

		Folder folder = folderValidator.findAndValidateFolderByUserId(folderId, userId);
		Optional<Itinerary> optionalItinerary = itineraryRepository.findByFolder(folder);

		if (optionalItinerary.isPresent()) {
			Itinerary itinerary = optionalItinerary.get();
			List<ItineraryPlace> itineraryPlaces = loadItineraryPlacesWithPlace(itinerary);

			// 경로가 있고 방문 순서가 설정되어 있으면 반환
			if (hasValidOrder(itineraryPlaces)) {
				List<FolderPlaceResponseDto> result = buildRouteDetailsWithWalkingInfo(itineraryPlaces);
				log.info("경로 상세 조회 완료 - folderId: {}, placeCount: {}", folderId, result.size());
				return result;
			}
		}

		// 경로가 없거나 비어있으면 빈 리스트 반환
		log.info("경로 상세 없음 - folderId: {}", folderId);
		return List.of();
	}

	// 경로 상세 정보에 도보 경로 정보 추가 빌드
	private List<FolderPlaceResponseDto> buildRouteDetailsWithWalkingInfo(List<ItineraryPlace> itineraryPlaces) {
		List<ItineraryPlace> sortedPlaces = itineraryPlaces.stream()
				.sorted(Comparator.comparing(ip -> ip.getVisitOrder() == null ? 0 : ip.getVisitOrder()))
				.collect(Collectors.toList());

		List<FolderPlaceResponseDto> result = new ArrayList<>();

		for (int i = 0; i < sortedPlaces.size(); i++) {
			ItineraryPlace current = sortedPlaces.get(i);
			FolderPlace fp = new FolderPlace();
			fp.setPlace(current.getPlace());
			FolderPlaceResponseDto dto = new FolderPlaceResponseDto(fp);

			// 다음 장소가 있으면 도보 경로 정보 추가
			if (i < sortedPlaces.size() - 1) {
				ItineraryPlace next = sortedPlaces.get(i + 1);
				addWalkingRouteInfo(dto, current.getPlace(), next.getPlace());
			}

			result.add(dto);
		}

		return result;
	}

	// 두 장소 간의 도보 경로 정보를 DTO에 추가
	private void addWalkingRouteInfo(FolderPlaceResponseDto dto, PlaceEntity from, PlaceEntity to) {
		if (from.getLatitude() == null || from.getLongitude() == null ||
				to.getLatitude() == null || to.getLongitude() == null) {
			return;
		}

		try {
			TmapPedestrianResponse response = tmapWalkingApiClient.getPedestrianRoute(
					from.getLongitude(), from.getLatitude(),
					to.getLongitude(), to.getLatitude()
			);

			if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
				// 첫 번째 Feature에 총 거리와 시간 정보
				TmapFeature firstFeature = response.getFeatures().get(0);
				if (firstFeature.getProperties() != null) {
					dto.setDistanceToNext(firstFeature.getProperties().getTotalDistance());
					dto.setDurationToNext(firstFeature.getProperties().getTotalTime());
					log.debug("경로 정보 추가: {} -> {} (거리: {}m, 시간: {}초)",
							from.getPlaceName(), to.getPlaceName(),
							firstFeature.getProperties().getTotalDistance(),
							firstFeature.getProperties().getTotalTime());
				}
			}
		} catch (Exception e) {
			log.warn("도보 경로 정보 조회 실패: {} -> {} ({})",
					from.getPlaceName(), to.getPlaceName(), e.getMessage());
		}
	}

	// 사용자가 수동으로 변경한 일정 순서를 저장
	@Transactional
	public void updateItineraryOrder(Integer folderId, List<Integer> placeIds, Long userId) {
		log.info("경로 순서 업데이트 시작 - folderId: {}, userId: {}, placeIds: {}", folderId, userId, placeIds);

		Folder folder = folderValidator.findAndValidateFolderByUserId(folderId, userId);
		List<FolderPlace> folderPlaces = loadFolderPlacesWithPlace(folder);

		// 유효성 검증
		validatePlaceIdsForUpdate(folderPlaces, placeIds);

		// Itinerary 생성 또는 조회
		Itinerary itinerary = getOrCreateItinerary(folder);

		// 순서 업데이트
		updatePlaceOrder(itinerary, placeIds);

		log.info("경로 순서 업데이트 완료 - folderId: {}", folderId);
	}

	// 업데이트할 placeIds 유효성 검증
	private void validatePlaceIdsForUpdate(List<FolderPlace> folderPlaces, List<Integer> placeIds) {
		if (folderPlaces.isEmpty()) {
			throw new InvalidFolderRequestException("폴더에 장소가 없습니다.");
		}

		if (placeIds == null || placeIds.isEmpty()) {
			throw new InvalidFolderRequestException("순서를 변경할 장소 목록이 비어있습니다.");
		}

		List<Integer> folderPlaceIds = folderPlaces.stream()
				.map(fp -> fp.getPlace().getPlaceId())
				.collect(Collectors.toList());

		// 요청된 장소들이 모두 폴더에 있는지 검증
		for (Integer placeId : placeIds) {
			if (!folderPlaceIds.contains(placeId)) {
				throw new InvalidFolderRequestException("폴더에 없는 장소가 포함되어 있습니다. placeId: " + placeId);
			}
		}

		// 중복된 placeId가 있는지 확인
		long distinctCount = placeIds.stream().distinct().count();
		if (distinctCount != placeIds.size()) {
			throw new InvalidFolderRequestException("중복된 장소가 포함되어 있습니다.");
		}

		log.info("순서 변경 검증 완료 - 폴더 장소 수: {}, 순서 변경 대상: {}", folderPlaceIds.size(), placeIds.size());
	}

	// Itinerary 생성 또는 조회
	private Itinerary getOrCreateItinerary(Folder folder) {
		return itineraryRepository.findByFolder(folder)
				.orElseGet(() -> {
					Itinerary it = new Itinerary();
					it.setFolder(folder);
					it.setItineraryGeneratedByAi(false);
					return itineraryRepository.save(it);
				});
	}

	// 장소 순서 업데이트
	private void updatePlaceOrder(Itinerary itinerary, List<Integer> placeIds) {
		List<ItineraryPlace> existingPlaces = loadItineraryPlacesWithPlace(itinerary);

		Map<Integer, ItineraryPlace> currentMap;
		try {
			currentMap = existingPlaces.stream()
					.collect(Collectors.toMap(
							ip -> ip.getPlace().getPlaceId(),
							ip -> ip,
							(existing, replacement) -> existing));
		} catch (IllegalStateException e) {
			log.error("중복된 placeId 발견 - itineraryId: {}", itinerary.getItineraryId(), e);
			throw new InvalidFolderRequestException("경로에 중복된 장소가 있습니다.");
		}

		// 요청된 장소들의 visitOrder 재설정
		int order = 1;
		for (Integer placeId : placeIds) {
			ItineraryPlace ip = currentMap.get(placeId);
			if (ip == null) {
				// 새로운 장소 추가
				ip = createItineraryPlace(itinerary, placeId, order);
			} else {
				// 기존 장소 순서 업데이트
				ip.setVisitOrder(order);
			}
			itineraryPlaceRepository.save(ip);
			order++;
		}

		// 요청에 포함되지 않은 장소들은 순서를 null로 설정하여 맨 뒤로 이동
		for (ItineraryPlace ip : existingPlaces) {
			if (!placeIds.contains(ip.getPlace().getPlaceId())) {
				ip.setVisitOrder(null);
				itineraryPlaceRepository.save(ip);
				log.debug("장소 순서 초기화 (맨 뒤로 이동) - placeId: {}", ip.getPlace().getPlaceId());
			}
		}

		log.info("경로 순서 업데이트 완료 - 순서 지정: {}개, 순서 미지정: {}개",
				placeIds.size(), existingPlaces.size() - placeIds.size());
	}

	// ItineraryPlace 생성
	private ItineraryPlace createItineraryPlace(Itinerary itinerary, Integer placeId, int order) {
		PlaceEntity place = placeRepository.findById(placeId)
				.orElseThrow(() -> new InvalidFolderRequestException("해당 장소를 찾을 수 없습니다. id=" + placeId));

		ItineraryPlace ip = new ItineraryPlace();
		ip.setItinerary(itinerary);
		ip.setPlace(place);
		ip.setVisitOrder(order);
		return ip;
	}

	// 사용되지 않는 ItineraryPlace 제거
	private void removeUnusedItineraryPlaces(List<ItineraryPlace> existingPlaces, List<Integer> placeIds) {
		for (ItineraryPlace ip : existingPlaces) {
			if (!placeIds.contains(ip.getPlace().getPlaceId())) {
				itineraryPlaceRepository.delete(ip);
			}
		}
	}

	// 경로 추천 재생성 - 거리 기반 최적 경로 생성
	@Transactional
	public List<FolderPlaceResponseDto> regenerateRecommendedRoute(Integer folderId, Long userId) {
		log.info("경로 재생성 시작 - folderId: {}, userId: {}", folderId, userId);

		Folder folder = folderValidator.findAndValidateFolderByUserId(folderId, userId);
		List<FolderPlace> folderPlaces = loadFolderPlacesWithPlace(folder);

		if (folderPlaces.isEmpty()) {
			throw new InvalidFolderRequestException("폴더에 장소가 없습니다. 경로를 생성할 수 없습니다.");
		}

		// 유효한 장소 추출
		List<PlaceEntity> places = extractValidPlaces(folderPlaces);
		if (places.isEmpty()) {
			throw new InvalidFolderRequestException("유효한 장소가 없습니다. 경로를 생성할 수 없습니다.");
		}

		// 최적 경로 생성
		List<Integer> optimalRoute = routeOptimizationService.generateOptimalRoute(places);

		// Itinerary 생성 또는 조회
		Itinerary itinerary = getOrCreateItinerary(folder);

		// 경로 저장
		saveOptimalRoute(itinerary, optimalRoute);

		// 영속성 컨텍스트 플러시
		entityManager.flush();

		// 최종 경로 반환 (도보 경로 정보 포함)
		List<FolderPlaceResponseDto> result = buildFinalRouteDetails(itinerary);
		log.info("경로 재생성 완료 - folderId: {}, placeCount: {}", folderId, result.size());

		return result;
	}

	// 유효한 장소 추출 (placeId가 null이 아니고 0이 아닌 장소)
	private List<PlaceEntity> extractValidPlaces(List<FolderPlace> folderPlaces) {
		return folderPlaces.stream()
				.map(FolderPlace::getPlace)
				.filter(place -> place != null && place.getPlaceId() != null && place.getPlaceId() != 0)
				.collect(Collectors.toList());
	}

	// 최적 경로 저장
	private void saveOptimalRoute(Itinerary itinerary, List<Integer> placeIds) {
		List<ItineraryPlace> existingPlaces = loadItineraryPlacesWithPlace(itinerary);

		Map<Integer, ItineraryPlace> existingMap;
		try {
			existingMap = existingPlaces.stream()
					.collect(Collectors.toMap(
							ip -> ip.getPlace().getPlaceId(),
							ip -> ip,
							(existing, replacement) -> existing));
		} catch (IllegalStateException e) {
			log.error("중복된 placeId 발견 - itineraryId: {}", itinerary.getItineraryId(), e);
			throw new InvalidFolderRequestException("경로에 중복된 장소가 있습니다.");
		}

		// 새로운 ItineraryPlace 생성 및 visitOrder 설정
		int order = 1;
		for (Integer placeId : placeIds) {
			if (placeId == null || placeId == 0) {
				log.warn("유효하지 않은 placeId 발견: {}, 건너뜁니다.", placeId);
				continue;
			}

			ItineraryPlace ip = existingMap.get(placeId);
			if (ip == null) {
				// 기존에 없으면 새로 생성
				ip = createItineraryPlace(itinerary, placeId, order);
			} else {
				// 기존에 있으면 visitOrder만 업데이트
				ip.setVisitOrder(order);
			}
			itineraryPlaceRepository.save(ip);
			order++;
		}

		// 새로운 경로에 포함되지 않은 기존 ItineraryPlace는 삭제
		removeUnusedItineraryPlaces(existingPlaces, placeIds);
	}

	// 최종 경로 상세 정보 빌드 (도보 경로 정보 포함)
	private List<FolderPlaceResponseDto> buildFinalRouteDetails(Itinerary itinerary) {
		List<ItineraryPlace> sortedPlaces = itineraryPlaceRepository.findByItinerary(itinerary).stream()
				.sorted(Comparator.comparing(ip -> ip.getVisitOrder() == null ? 0 : ip.getVisitOrder()))
				.collect(Collectors.toList());

		return buildRouteDetailsWithWalkingInfo(sortedPlaces);
	}


	// ItineraryPlace 조회
	private List<ItineraryPlace> loadItineraryPlacesWithPlace(Itinerary itinerary) {
		List<ItineraryPlace> itineraryPlaces = itineraryPlaceRepository.findByItinerary(itinerary);

		// LAZY 로딩 초기화
		for (ItineraryPlace ip : itineraryPlaces) {
			ip.getPlace().getPlaceId();
		}

		return itineraryPlaces;
	}

	// FolderPlace 조회
	private List<FolderPlace> loadFolderPlacesWithPlace(Folder folder) {
		List<FolderPlace> folderPlaces = folderPlaceRepository.findAllByFolder(folder);

		// LAZY 로딩 초기화
		for (FolderPlace fp : folderPlaces) {
			fp.getPlace().getPlaceId();
		}

		return folderPlaces;
	}

	// 유효한 방문 순서가 있는지 확인
	private boolean hasValidOrder(List<ItineraryPlace> itineraryPlaces) {
		return !itineraryPlaces.isEmpty() &&
				itineraryPlaces.stream().anyMatch(ip -> ip.getVisitOrder() != null);
	}
}

