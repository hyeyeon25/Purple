package Purple.Purple.itinerery.service;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.folder.domain.FolderPlace;
import Purple.Purple.folder.repository.FolderPlaceRepository;
import Purple.Purple.folder.repository.FolderRepository;
import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.itinerery.domain.ItineraryPlace;
import Purple.Purple.itinerery.dto.RouteUpdateRequestDto;
import Purple.Purple.itinerery.repository.ItineraryPlaceRepository;
import Purple.Purple.itinerery.repository.ItineraryRepository;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.place.repository.PlaceRepository;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

	private final FolderRepository folderRepository;
	private final FolderPlaceRepository folderPlaceRepository;
	private final ItineraryRepository itineraryRepository;
	private final ItineraryPlaceRepository itineraryPlaceRepository;
	private final UserRepository userRepository;
	private final PlaceRepository placeRepository;

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

		// 폴더에 있는 장소들을 ID 순서대로 자동 경로 생성
		List<Integer> placeIds = folderPlaces.stream()
				.map(fp -> fp.getPlace().getPlaceId())
				.sorted()
				.collect(Collectors.toList());

		// Itinerary 생성 또는 가져오기
		Itinerary itinerary = optionalItinerary.orElseGet(() -> {
			Itinerary it = new Itinerary();
			it.setFolder(folder);
			return itineraryRepository.save(it);
		});

		// 기존 ItineraryPlace 조회
		List<ItineraryPlace> existingPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
		Map<Integer, ItineraryPlace> existingMap = existingPlaces.stream()
				.collect(Collectors.toMap(ip -> ip.getPlace().getPlaceId(), ip -> ip));

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
	public void putRoute(Integer folderId, RouteUpdateRequestDto requestDto, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));

		List<Integer> placeIds = requestDto.getPlaceIdsInOrder();
		
		// placeIdsInOrder가 없거나 비어있으면 폴더에 있는 모든 장소로 자동 경로 생성
		if (placeIds == null || placeIds.isEmpty()) {
			List<FolderPlace> folderPlaces = folderPlaceRepository.findAllByFolder(folder);
			if (folderPlaces.isEmpty()) {
				throw new IllegalArgumentException("폴더에 장소가 없습니다. 경로를 생성할 수 없습니다.");
			}
			// 폴더에 있는 장소들을 ID 순서대로 자동 경로 생성
			placeIds = folderPlaces.stream()
					.map(fp -> fp.getPlace().getPlaceId())
					.sorted()
					.collect(Collectors.toList());
		}

		// 폴더에 없는 장소는 자동 추가
		for (Integer placeId : placeIds) {
			PlaceEntity place = placeRepository.findById(placeId)
					.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + placeId));
			if (!folderPlaceRepository.existsByFolderAndPlace(folder, place)) {
				FolderPlace fp = new FolderPlace();
				fp.setFolder(folder);
				fp.setPlace(place);
				folderPlaceRepository.save(fp);
			}
		}

		// 폴더당 단일 경로(Itinerary) 보장
		Itinerary itinerary = itineraryRepository.findByFolder(folder)
				.orElseGet(() -> {
					Itinerary it = new Itinerary();
					it.setFolder(folder);
					return itineraryRepository.save(it);
				});

		// 기존 ItineraryPlace 조회 (LAZY 로딩 문제 해결)
		List<ItineraryPlace> existingItineraryPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
		Map<Integer, ItineraryPlace> currentMap = existingItineraryPlaces.stream()
				.collect(Collectors.toMap(ip -> ip.getPlace().getPlaceId(), ip -> ip));

		// visitOrder 재설정
		int order = 1;
		for (Integer placeId : placeIds) {
			ItineraryPlace ip = currentMap.get(placeId);
			if (ip == null) {
				ip = new ItineraryPlace();
				ip.setItinerary(itinerary);
				PlaceEntity place = placeRepository.findById(placeId).orElseThrow();
				ip.setPlace(place);
				itineraryPlaceRepository.save(ip);
			}
			ip.setVisitOrder(order++);
		}

		// 입력에 없는 기존 경로 아이템은 제거
		for (ItineraryPlace ip : existingItineraryPlaces) {
			if (!placeIds.contains(ip.getPlace().getPlaceId())) {
				itineraryPlaceRepository.delete(ip);
			}
		}
	}
}


