package Purple.Purple.itinerery.service;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.folder.domain.FolderPlace;
import Purple.Purple.folder.dto.FolderPlaceResponseDto;
import Purple.Purple.folder.repository.FolderPlaceRepository;
import Purple.Purple.folder.repository.FolderRepository;
import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.itinerery.domain.ItineraryPlace;
import Purple.Purple.itinerery.repository.ItineraryPlaceRepository;
import Purple.Purple.itinerery.repository.ItineraryRepository;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.place.repository.PlaceRepository;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.repository.UserRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryService {

	private final FolderRepository folderRepository;
	private final FolderPlaceRepository folderPlaceRepository;
	private final ItineraryRepository itineraryRepository;
	private final ItineraryPlaceRepository itineraryPlaceRepository;
	private final UserRepository userRepository;
	private final PlaceRepository placeRepository;
	private final EntityManager entityManager;

	@Transactional
	public List<Integer> getRoute(Integer folderId, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));

		// 기존 경로 조회
		Optional<Itinerary> optionalItinerary = itineraryRepository.findByFolder(folder);
		
		if (optionalItinerary.isPresent()) {
			Itinerary itinerary = optionalItinerary.get();
			// LAZY 로딩 문제 해결: Repository에서 직접 조회
			List<ItineraryPlace> itineraryPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
			
			// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
			for (ItineraryPlace ip : itineraryPlaces) {
				ip.getPlace().getPlaceId(); // Place 초기화
			}
			
			// 경로가 있고 방문 순서가 설정되어 있으면 반환
			if (!itineraryPlaces.isEmpty() && itineraryPlaces.stream()
					.anyMatch(ip -> ip.getVisitOrder() != null)) {
				return itineraryPlaces.stream()
						.sorted(Comparator.comparing(ip -> ip.getVisitOrder() == null ? 0 : ip.getVisitOrder()))
						.map(ip -> ip.getPlace().getPlaceId())
						.collect(Collectors.toList());
			}
		}

		// 경로가 없거나 비어있으면 자동 생성
		List<FolderPlace> folderPlaces = folderPlaceRepository.findAllByFolder(folder);
		if (folderPlaces.isEmpty()) {
			return List.of();
		}

		// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
		for (FolderPlace fp : folderPlaces) {
			fp.getPlace().getPlaceId(); // Place 초기화
		}

		// 폴더에 있는 장소들을 거리 기반 최적 경로로 자동 생성
		List<PlaceEntity> places = folderPlaces.stream()
				.map(FolderPlace::getPlace)
				.collect(Collectors.toList());
		
		List<Integer> placeIds = generateOptimalRoute(places);

		// Itinerary 생성 또는 가져오기
		Itinerary itinerary = optionalItinerary.orElseGet(() -> {
			Itinerary it = new Itinerary();
			it.setFolder(folder);
			it.setItineraryGeneratedByAi(false);
			return itineraryRepository.save(it);
		});

		// 기존 ItineraryPlace 조회
		List<ItineraryPlace> existingPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
		
		// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
		for (ItineraryPlace ip : existingPlaces) {
			ip.getPlace().getPlaceId(); // Place 초기화
		}
		
		Map<Integer, ItineraryPlace> existingMap;
		try {
			existingMap = existingPlaces.stream()
					.collect(Collectors.toMap(ip -> ip.getPlace().getPlaceId(), ip -> ip, (existing, replacement) -> existing));
		} catch (IllegalStateException e) {
			log.error("중복된 placeId가 발견되었습니다. folderId: {}", folderId, e);
			throw new IllegalArgumentException("경로에 중복된 장소가 있습니다.", e);
		}

		// ItineraryPlace 생성 및 visitOrder 설정
		int order = 1;
		for (Integer placeId : placeIds) {
			PlaceEntity place = placeRepository.findById(placeId)
					.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + placeId));
			
			ItineraryPlace ip = existingMap.get(placeId);
			if (ip == null) {
				ip = new ItineraryPlace();
				ip.setItinerary(itinerary);
				ip.setPlace(place);
				ip.setVisitOrder(order);
				itineraryPlaceRepository.save(ip);
			} else {
				ip.setVisitOrder(order);
				itineraryPlaceRepository.save(ip);
			}
			order++;
		}

		// 폴더에 없는 기존 경로 아이템은 제거
		for (ItineraryPlace ip : existingPlaces) {
			if (!placeIds.contains(ip.getPlace().getPlaceId())) {
				itineraryPlaceRepository.delete(ip);
			}
		}

		return placeIds;
	}

	@Transactional
	public List<FolderPlaceResponseDto> getRouteDetails(Integer folderId, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));

		// 기존 경로 조회
		Optional<Itinerary> optionalItinerary = itineraryRepository.findByFolder(folder);
		
		if (optionalItinerary.isPresent()) {
			Itinerary itinerary = optionalItinerary.get();
			List<ItineraryPlace> itineraryPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
			
			// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
			for (ItineraryPlace ip : itineraryPlaces) {
				ip.getPlace().getPlaceId(); // Place 초기화
			}
			
			// 경로가 있고 방문 순서가 설정되어 있으면 반환
			if (!itineraryPlaces.isEmpty() && itineraryPlaces.stream()
					.anyMatch(ip -> ip.getVisitOrder() != null)) {
				return itineraryPlaces.stream()
						.sorted(Comparator.comparing(ip -> ip.getVisitOrder() == null ? 0 : ip.getVisitOrder()))
						.map(ip -> {
							FolderPlace fp = new FolderPlace();
							fp.setPlace(ip.getPlace());
							return new FolderPlaceResponseDto(fp);
						})
						.collect(Collectors.toList());
			}
		}

		// 경로가 없거나 비어있으면 자동 생성
		List<FolderPlace> folderPlaces = folderPlaceRepository.findAllByFolder(folder);
		if (folderPlaces.isEmpty()) {
			return List.of();
		}

		// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
		for (FolderPlace fp : folderPlaces) {
			fp.getPlace().getPlaceId(); // Place 초기화
		}

		// 폴더에 있는 장소들을 거리 기반 최적 경로로 자동 생성
		List<PlaceEntity> places = folderPlaces.stream()
				.map(FolderPlace::getPlace)
				.collect(Collectors.toList());
		
		List<Integer> placeIds = generateOptimalRoute(places);

		// Itinerary 생성 또는 가져오기
		Itinerary itinerary = optionalItinerary.orElseGet(() -> {
			Itinerary it = new Itinerary();
			it.setFolder(folder);
			it.setItineraryGeneratedByAi(false);
			return itineraryRepository.save(it);
		});

		// 기존 ItineraryPlace 조회
		List<ItineraryPlace> existingPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
		
		// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
		for (ItineraryPlace ip : existingPlaces) {
			ip.getPlace().getPlaceId(); // Place 초기화
		}
		
		Map<Integer, ItineraryPlace> existingMap;
		try {
			existingMap = existingPlaces.stream()
					.collect(Collectors.toMap(ip -> ip.getPlace().getPlaceId(), ip -> ip, (existing, replacement) -> existing));
		} catch (IllegalStateException e) {
			log.error("중복된 placeId가 발견되었습니다. folderId: {}", folderId, e);
			throw new IllegalArgumentException("경로에 중복된 장소가 있습니다.", e);
		}

		// ItineraryPlace 생성 및 visitOrder 설정
		int order = 1;
		for (Integer placeId : placeIds) {
			PlaceEntity place = placeRepository.findById(placeId)
					.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + placeId));
			
			ItineraryPlace ip = existingMap.get(placeId);
			if (ip == null) {
				ip = new ItineraryPlace();
				ip.setItinerary(itinerary);
				ip.setPlace(place);
				ip.setVisitOrder(order);
				itineraryPlaceRepository.save(ip);
			} else {
				ip.setVisitOrder(order);
				itineraryPlaceRepository.save(ip);
			}
			order++;
		}

		// 폴더에 없는 기존 경로 아이템은 제거
		for (ItineraryPlace ip : existingPlaces) {
			if (!placeIds.contains(ip.getPlace().getPlaceId())) {
				itineraryPlaceRepository.delete(ip);
			}
		}

		// 최종 경로를 FolderPlaceResponseDto 리스트로 변환
		return itineraryPlaceRepository.findByItinerary(itinerary).stream()
				.sorted(Comparator.comparing(ip -> ip.getVisitOrder() == null ? 0 : ip.getVisitOrder()))
				.map(ip -> {
					FolderPlace fp = new FolderPlace();
					fp.setPlace(ip.getPlace());
					return new FolderPlaceResponseDto(fp);
				})
				.collect(Collectors.toList());
	}

	/**
	 * 사용자가 수동으로 변경한 일정 순서를 저장합니다.
	 * @param folderId 폴더 ID
	 * @param placeIds 방문 순서대로 정렬된 장소 ID 리스트
	 * @param userId 사용자 ID
	 */
	@Transactional
	public void updateItineraryOrder(Integer folderId, List<Integer> placeIds, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));

		// 폴더에 있는 장소들 조회
		List<FolderPlace> folderPlaces = folderPlaceRepository.findAllByFolder(folder);
		if (folderPlaces.isEmpty()) {
			throw new IllegalArgumentException("폴더에 장소가 없습니다.");
		}
		
		// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
		for (FolderPlace fp : folderPlaces) {
			fp.getPlace().getPlaceId(); // Place 초기화
		}
		
		// 폴더에 있는 장소 ID 목록
		List<Integer> folderPlaceIds = folderPlaces.stream()
				.map(fp -> fp.getPlace().getPlaceId())
				.collect(Collectors.toList());
		
		// 폴더에 있는 장소인지 검증
		for (Integer placeId : placeIds) {
			if (!folderPlaceIds.contains(placeId)) {
				throw new IllegalArgumentException("폴더에 없는 장소가 포함되어 있습니다. placeId: " + placeId);
			}
		}
		
		// 폴더의 모든 장소가 포함되었는지 확인
		if (placeIds.size() != folderPlaceIds.size() || !folderPlaceIds.containsAll(placeIds)) {
			throw new IllegalArgumentException("폴더의 모든 장소를 포함해야 하며, 폴더에 없는 장소는 포함할 수 없습니다.");
		}

		// 폴더당 단일 경로(Itinerary) 보장
		Itinerary itinerary = itineraryRepository.findByFolder(folder)
				.orElseGet(() -> {
					Itinerary it = new Itinerary();
					it.setFolder(folder);
					it.setItineraryGeneratedByAi(false);
					return itineraryRepository.save(it);
				});

		// 기존 ItineraryPlace 조회 (LAZY 로딩 문제 해결)
		List<ItineraryPlace> existingItineraryPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
		
		// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
		for (ItineraryPlace ip : existingItineraryPlaces) {
			ip.getPlace().getPlaceId(); // Place 초기화
		}
		
		Map<Integer, ItineraryPlace> currentMap;
		try {
			currentMap = existingItineraryPlaces.stream()
					.collect(Collectors.toMap(ip -> ip.getPlace().getPlaceId(), ip -> ip, (existing, replacement) -> existing));
		} catch (IllegalStateException e) {
			log.error("중복된 placeId가 발견되었습니다. folderId: {}", folderId, e);
			throw new IllegalArgumentException("경로에 중복된 장소가 있습니다.", e);
		}

		// visitOrder 재설정
		int order = 1;
		for (Integer placeId : placeIds) {
			ItineraryPlace ip = currentMap.get(placeId);
			if (ip == null) {
				ip = new ItineraryPlace();
				ip.setItinerary(itinerary);
				PlaceEntity place = placeRepository.findById(placeId)
						.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + placeId));
				ip.setPlace(place);
				ip.setVisitOrder(order);
				itineraryPlaceRepository.save(ip);
			} else {
				ip.setVisitOrder(order);
				itineraryPlaceRepository.save(ip);
			}
			order++;
		}

		// 입력에 없는 기존 경로 아이템은 제거
		for (ItineraryPlace ip : existingItineraryPlaces) {
			if (!placeIds.contains(ip.getPlace().getPlaceId())) {
				itineraryPlaceRepository.delete(ip);
			}
		}
	}

	/**
	 * 경로 추천 재생성 - 기존 경로를 무시하고 거리 기반 최적 경로를 새로 생성합니다.
	 * @param folderId 폴더 ID
	 * @param userId 사용자 ID
	 * @return 새로 생성된 경로의 상세 정보
	 */
	@Transactional
	public List<FolderPlaceResponseDto> regenerateRecommendedRoute(Integer folderId, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));

		// 폴더에 있는 장소들 조회
		List<FolderPlace> folderPlaces = folderPlaceRepository.findAllByFolder(folder);
		if (folderPlaces.isEmpty()) {
			throw new IllegalArgumentException("폴더에 장소가 없습니다. 경로를 생성할 수 없습니다.");
		}

		// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
		for (FolderPlace fp : folderPlaces) {
			fp.getPlace().getPlaceId(); // Place 초기화
		}

		// 폴더에 있는 장소들을 거리 기반 최적 경로로 자동 생성
		List<PlaceEntity> places = folderPlaces.stream()
				.map(FolderPlace::getPlace)
				.collect(Collectors.toList());
		List<Integer> placeIds = generateOptimalRoute(places);

		// 폴더당 단일 경로(Itinerary) 보장
		Itinerary itinerary = itineraryRepository.findByFolder(folder)
				.orElseGet(() -> {
					Itinerary it = new Itinerary();
					it.setFolder(folder);
					it.setItineraryGeneratedByAi(false);
					return itineraryRepository.save(it);
				});

		// 기존 ItineraryPlace 조회
		List<ItineraryPlace> existingItineraryPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
		
		// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
		for (ItineraryPlace ip : existingItineraryPlaces) {
			ip.getPlace().getPlaceId(); // Place 초기화
		}
		
		// 기존 ItineraryPlace를 Map으로 변환 (placeId -> ItineraryPlace)
		Map<Integer, ItineraryPlace> existingMap;
		try {
			existingMap = existingItineraryPlaces.stream()
					.collect(Collectors.toMap(ip -> ip.getPlace().getPlaceId(), ip -> ip, (existing, replacement) -> existing));
		} catch (IllegalStateException e) {
			log.error("중복된 placeId가 발견되었습니다. folderId: {}", folderId, e);
			throw new IllegalArgumentException("경로에 중복된 장소가 있습니다.", e);
		}

		// 새로운 ItineraryPlace 생성 및 visitOrder 설정
		int order = 1;
		for (Integer placeId : placeIds) {
			PlaceEntity place = placeRepository.findById(placeId)
					.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + placeId));
			
			ItineraryPlace ip = existingMap.get(placeId);
			if (ip == null) {
				// 기존에 없으면 새로 생성
				ip = new ItineraryPlace();
				ip.setItinerary(itinerary);
				ip.setPlace(place);
				ip.setVisitOrder(order);
				itineraryPlaceRepository.save(ip);
			} else {
				// 기존에 있으면 visitOrder만 업데이트
				ip.setVisitOrder(order);
				itineraryPlaceRepository.save(ip);
			}
			order++;
		}

		// 새로운 경로에 포함되지 않은 기존 ItineraryPlace는 삭제
		for (ItineraryPlace ip : existingItineraryPlaces) {
			if (!placeIds.contains(ip.getPlace().getPlaceId())) {
				itineraryPlaceRepository.delete(ip);
			}
		}
		
		// 영속성 컨텍스트를 플러시하여 DB에 즉시 반영
		entityManager.flush();

		// 최종 경로를 FolderPlaceResponseDto 리스트로 변환하여 반환
		return itineraryPlaceRepository.findByItinerary(itinerary).stream()
				.sorted(Comparator.comparing(ip -> ip.getVisitOrder() == null ? 0 : ip.getVisitOrder()))
				.map(ip -> {
					FolderPlace fp = new FolderPlace();
					fp.setPlace(ip.getPlace());
					return new FolderPlaceResponseDto(fp);
				})
				.collect(Collectors.toList());
	}

	/**
	 * 거리 기반 최적 경로 생성 (Nearest Neighbor 휴리스틱)
	 * @param places 장소 리스트
	 * @return 최적 순서의 장소 ID 리스트
	 */
	private List<Integer> generateOptimalRoute(List<PlaceEntity> places) {
		if (places.isEmpty()) {
			return List.of();
		}
		if (places.size() == 1) {
			return List.of(places.get(0).getPlaceId());
		}

		log.info("=== 최적 경로 생성 시작 (총 {}개 장소) ===", places.size());
		
		// Nearest Neighbor 알고리즘으로 최적 경로 생성
		List<PlaceEntity> unvisited = new ArrayList<>(places);
		List<Integer> route = new ArrayList<>();
		double totalDistance = 0.0;
		
		// 첫 번째 장소 선택 (위도/경도가 있는 첫 번째 장소)
		PlaceEntity current = unvisited.stream()
				.filter(p -> p.getLatitude() != null && p.getLongitude() != null)
				.findFirst()
				.orElse(unvisited.get(0));
		
		route.add(current.getPlaceId());
		unvisited.remove(current);
		log.info("1. 시작 장소: {} (위도: {}, 경도: {})", 
				current.getPlaceName(), current.getLatitude(), current.getLongitude());
		
		// 가장 가까운 장소를 순차적으로 선택
		int order = 2;
		while (!unvisited.isEmpty()) {
			PlaceEntity nearest = null;
			double minDist = Double.MAX_VALUE;
			
			for (PlaceEntity place : unvisited) {
				if (place.getLatitude() == null || place.getLongitude() == null) {
					continue;
				}
				double dist = getDistance(current, place);
				if (dist < minDist) {
					minDist = dist;
					nearest = place;
				}
			}
			
			// 좌표가 없는 장소는 마지막에 추가
			if (nearest == null) {
				nearest = unvisited.get(0);
				minDist = 0.0;
			}
			
			route.add(nearest.getPlaceId());
			totalDistance += minDist;
			log.info("{}. 다음 장소: {} (거리: {:.2f}km, 누적: {:.2f}km)", 
					order++, nearest.getPlaceName(), minDist, totalDistance);
			
			unvisited.remove(nearest);
			current = nearest;
		}
		
		log.info("=== 최적 경로 생성 완료 (총 거리: {:.2f}km) ===", totalDistance);
		log.info("경로 순서: {}", route);
		
		return route;
	}

	/**
	 * 두 장소 간의 거리 계산 (Haversine 공식)
	 * @param a 첫 번째 장소
	 * @param b 두 번째 장소
	 * @return 거리 (km)
	 */
	private double getDistance(PlaceEntity a, PlaceEntity b) {
		if (a.getLatitude() == null || a.getLongitude() == null ||
				b.getLatitude() == null || b.getLongitude() == null) {
			return Double.MAX_VALUE; // 좌표가 없으면 매우 큰 값 반환
		}
		
		final int R = 6371; // 지구 반경 (km)
		double latDist = Math.toRadians(b.getLatitude() - a.getLatitude());
		double lonDist = Math.toRadians(b.getLongitude() - a.getLongitude());
		double hav = Math.sin(latDist / 2) * Math.sin(latDist / 2)
				+ Math.cos(Math.toRadians(a.getLatitude())) * Math.cos(Math.toRadians(b.getLatitude()))
				* Math.sin(lonDist / 2) * Math.sin(lonDist / 2);
		return R * 2 * Math.atan2(Math.sqrt(hav), Math.sqrt(1 - hav));
	}
}

