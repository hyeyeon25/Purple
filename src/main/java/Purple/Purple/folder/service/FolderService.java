package Purple.Purple.folder.service;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.folder.domain.FolderPlace;
import Purple.Purple.folder.repository.FolderPlaceRepository;
import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.itinerery.domain.ItineraryPlace;
import Purple.Purple.itinerery.repository.ItineraryPlaceRepository;
import Purple.Purple.itinerery.repository.ItineraryRepository;
import Purple.Purple.folder.repository.FolderRepository;
import Purple.Purple.folder.dto.FolderSummaryResponseDto;
import Purple.Purple.folder.dto.FolderUpdateRequestDto;
import Purple.Purple.folder.dto.FolderDetailResponseDto;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.place.repository.PlaceRepository;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.repository.UserRepository;
import Purple.Purple.folder.dto.FolderCreateRequestDto;
import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import Purple.Purple.Neighborhood.repository.NeighborhoodRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

	private final FolderRepository folderRepository;
	private final FolderPlaceRepository folderPlaceRepository;
	private final UserRepository userRepository;
	private final PlaceRepository placeRepository;
	private final NeighborhoodRepository neighborhoodRepository;
	private final ItineraryRepository itineraryRepository;
	private final ItineraryPlaceRepository itineraryPlaceRepository;

	@Transactional
	public Integer createFolder(FolderCreateRequestDto requestDto, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		
		// neighborhoodId가 0이거나 null인 경우 처리
		if (requestDto.getNeighborhoodId() == null || requestDto.getNeighborhoodId() == 0) {
			throw new IllegalArgumentException("유효한 동네 ID가 필요합니다. (neighborhoodId: " + requestDto.getNeighborhoodId() + ")");
		}
		
		NeighborhoodEntity neighborhood = neighborhoodRepository.findById(requestDto.getNeighborhoodId())
				.orElseThrow(() -> new IllegalArgumentException("해당 동네를 찾을 수 없습니다. id=" + requestDto.getNeighborhoodId()));
		
		// 폴더명 자동 생성: "{날짜} {동네명}" 형식
		// 예: "2025-01-15 불당동"
		// 사용자가 지정한 folderTitle은 무시하고 항상 자동 생성
		LocalDate date = requestDto.getDate();
		if (date == null) {
			throw new IllegalArgumentException("여행 날짜는 필수입니다.");
		}
		String folderTitle = formatDate(date) + " " + neighborhood.getNeighborhoodName();
		
		Folder folder = new Folder();
		folder.setUser(user);
		folder.setFolderTitle(folderTitle);
		folder.setDate(date);
		folder.setNeighborhoodId(requestDto.getNeighborhoodId());
		Folder saved = folderRepository.save(folder);
		
		// 선택한 장소들을 폴더에 추가 (사용자가 선택한 순서대로)
		if (requestDto.getPlaceIds() != null && !requestDto.getPlaceIds().isEmpty()) {
			// Itinerary 생성 (폴더 생성 시 순서 저장을 위해)
			Itinerary itinerary = new Itinerary();
			itinerary.setFolder(saved);
			itinerary.setItineraryGeneratedByAi(false);
			Itinerary savedItinerary = itineraryRepository.save(itinerary);
			
			int order = 1;
			for (Integer placeId : requestDto.getPlaceIds()) {
				// placeId가 0이거나 null이면 건너뛰기
				if (placeId == null || placeId == 0) {
					continue;
				}
				
				PlaceEntity place = placeRepository.findById(placeId)
						.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + placeId));
				
				// 중복 체크
				if (!folderPlaceRepository.existsByFolderAndPlace(saved, place)) {
					FolderPlace fp = new FolderPlace();
					fp.setFolder(saved);
					fp.setPlace(place);
					folderPlaceRepository.save(fp);
				}
				
				// ItineraryPlace 생성 (사용자가 선택한 순서대로 visitOrder 저장)
				ItineraryPlace ip = new ItineraryPlace();
				ip.setItinerary(savedItinerary);
				ip.setPlace(place);
				ip.setVisitOrder(order);
				itineraryPlaceRepository.save(ip);
				order++;
			}
		}
		
		return saved.getFolderId();
	}

	@Transactional
	public void deleteFolder(Integer folderId, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));
		folderRepository.delete(folder);
	}

	@Transactional
	public void updateFolder(Integer folderId, FolderUpdateRequestDto requestDto, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));
		if (requestDto.getFolderTitle() != null) {
			folder.setFolderTitle(requestDto.getFolderTitle());
		}
		if (requestDto.getDate() != null) {
			folder.setDate(requestDto.getDate());
		}
	}

	@Transactional
	public Integer addPlaceToFolder(Integer folderId, Integer placeId, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));
		
		// placeId로 장소 조회 (DB에 이미 존재하는 장소만 추가 가능)
		PlaceEntity place = placeRepository.findById(placeId)
				.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + placeId));
		
		// 폴더에 이미 담겨 있으면 중복 추가 방지
		if (folderPlaceRepository.existsByFolderAndPlace(folder, place)) {
			return folderId; // 이미 담겨 있음
		}
		
		// 폴더에 장소 추가
		FolderPlace fp = new FolderPlace();
		fp.setFolder(folder);
		fp.setPlace(place);
		folderPlaceRepository.save(fp);
		
		// Itinerary가 있으면 마지막 순서로 추가
		Itinerary itinerary = itineraryRepository.findByFolder(folder).orElse(null);
		if (itinerary != null) {
			// 기존 ItineraryPlace 조회하여 최대 visitOrder 찾기
			List<ItineraryPlace> existingPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
			
			// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
			for (ItineraryPlace ip : existingPlaces) {
				ip.getPlace().getPlaceId(); // Place 초기화
			}
			
			int maxOrder = existingPlaces.stream()
					.filter(ip -> ip.getVisitOrder() != null)
					.mapToInt(ItineraryPlace::getVisitOrder)
					.max()
					.orElse(0);
			
			// 이미 itinerary에 있는지 확인
			boolean alreadyExists = existingPlaces.stream()
					.anyMatch(ip -> ip.getPlace().getPlaceId().equals(placeId));
			
			if (!alreadyExists) {
				// 마지막 순서로 추가
				ItineraryPlace ip = new ItineraryPlace();
				ip.setItinerary(itinerary);
				ip.setPlace(place);
				ip.setVisitOrder(maxOrder + 1);
				itineraryPlaceRepository.save(ip);
			}
		}
		
		return folderId;
	}

	@Transactional
	public void removePlaceFromFolder(Integer folderId, Integer placeId, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));
		PlaceEntity place = placeRepository.findById(placeId)
				.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + placeId));
		FolderPlace fp = folderPlaceRepository.findByFolderAndPlace(folder, place)
				.orElseThrow(() -> new IllegalArgumentException("폴더에 없는 장소입니다."));
		
		// 폴더에서 장소 삭제
		folderPlaceRepository.delete(fp);
		
		// Itinerary가 있으면 경로에서도 삭제
		Itinerary itinerary = itineraryRepository.findByFolder(folder).orElse(null);
		if (itinerary != null) {
			List<ItineraryPlace> itineraryPlaces = itineraryPlaceRepository.findByItinerary(itinerary);
			
			// LAZY 로딩 문제 해결: Place 엔티티를 명시적으로 초기화
			for (ItineraryPlace ip : itineraryPlaces) {
				ip.getPlace().getPlaceId(); // Place 초기화
			}
			
			// 해당 장소를 찾아서 삭제
			ItineraryPlace toRemove = itineraryPlaces.stream()
					.filter(ip -> ip.getPlace().getPlaceId().equals(placeId))
					.findFirst()
					.orElse(null);
			
			if (toRemove != null) {
				itineraryPlaceRepository.delete(toRemove);
			}
		}
	}

	@Transactional(readOnly = true)
	public FolderDetailResponseDto getFolderDetails(Integer folderId, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));
		
		// LAZY 로딩을 위해 폴더 플레이스와 Place 엔티티를 미리 로드
		List<FolderPlace> folderPlaces = folder.getFolderPlaces();
		for (FolderPlace fp : folderPlaces) {
			// Place 엔티티를 명시적으로 초기화
			fp.getPlace().getPlaceId();
		}
		
		// Itinerary와 ItineraryPlace도 초기화
		if (folder.getItinerary() != null) {
			List<ItineraryPlace> itineraryPlaces = folder.getItinerary().getItineraryPlaces();
			for (ItineraryPlace ip : itineraryPlaces) {
				// Place 엔티티를 명시적으로 초기화
				ip.getPlace().getPlaceId();
			}
		}
		
		return new FolderDetailResponseDto(folder);
	}

	@Transactional
	public List<FolderSummaryResponseDto> listMyFolders(Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		return folderRepository.findAllByUserOrderByFolderCreatedAtDesc(user).stream()
				.map(FolderSummaryResponseDto::new)
				.collect(Collectors.toList());
	}

	/**
	 * 날짜를 "2025-01-15" 형식으로 포맷팅
	 */
	private String formatDate(LocalDate date) {
		if (date == null) {
			return "";
		}
		return date.toString(); // LocalDate의 기본 형식: "yyyy-MM-dd"
	}
}


