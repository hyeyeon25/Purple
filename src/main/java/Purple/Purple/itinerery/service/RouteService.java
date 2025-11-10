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

		return itineraryRepository.findByFolder(folder)
				.map(Itinerary::getItineraryPlaces)
				.orElse(List.of())
				.stream()
				.sorted(Comparator.comparing(ip -> ip.getVisitOrder() == null ? 0 : ip.getVisitOrder()))
				.map(ip -> ip.getPlace().getPlaceId())
				.collect(Collectors.toList());
	}

	@Transactional
	public void putRoute(Integer folderId, RouteUpdateRequestDto requestDto, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));

		List<Integer> placeIds = requestDto.getPlaceIdsInOrder();
		if (placeIds == null || placeIds.isEmpty()) {
			throw new IllegalArgumentException("경로에 최소 1개 이상의 장소가 필요합니다.");
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

		// 기존 맵
		Map<Integer, ItineraryPlace> currentMap = itinerary.getItineraryPlaces().stream()
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
		for (ItineraryPlace ip : itinerary.getItineraryPlaces()) {
			if (!placeIds.contains(ip.getPlace().getPlaceId())) {
				itineraryPlaceRepository.delete(ip);
			}
		}
	}
}


