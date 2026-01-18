package Purple.Purple.folder.service;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.folder.domain.FolderPlace;
import Purple.Purple.folder.exception.FolderNotFoundException;
import Purple.Purple.folder.exception.InvalidFolderRequestException;
import Purple.Purple.folder.repository.FolderRepository;
import Purple.Purple.folder.dto.FolderSummaryResponseDto;
import Purple.Purple.folder.dto.FolderUpdateRequestDto;
import Purple.Purple.folder.dto.FolderDetailResponseDto;
import Purple.Purple.folder.dto.FolderCreateRequestDto;
import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.itinerery.domain.ItineraryPlace;
import Purple.Purple.itinerery.repository.ItineraryRepository;
import Purple.Purple.itinerery.repository.ItineraryPlaceRepository;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// 여행 폴더 관리 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

	private final FolderRepository folderRepository;
	private final ItineraryRepository itineraryRepository;
	private final ItineraryPlaceRepository itineraryPlaceRepository;
	private final FolderValidator folderValidator;
	private final FolderPlaceService folderPlaceService;

	// 폴더 생성
	@Transactional
	public Integer createFolder(FolderCreateRequestDto requestDto, Long userId) {
		log.info("폴더 생성 시작 - userId: {}, date: {}, neighborhoodId: {}",
				userId, requestDto.getDate(), requestDto.getNeighborhoodId());

		// 유효성 검증
		folderValidator.validateCreateRequest(requestDto);
		UserPersonalInfo user = folderValidator.validateUser(userId);
		NeighborhoodEntity neighborhood = folderValidator.validateNeighborhoodId(requestDto.getNeighborhoodId());

		// 폴더 생성
		Folder folder = buildFolder(user, requestDto, neighborhood);
		Folder savedFolder = folderRepository.save(folder);
		log.info("폴더 생성 완료 - folderId: {}, title: {}", savedFolder.getFolderId(), savedFolder.getFolderTitle());

		// Itinerary 생성
		Itinerary itinerary = createItinerary(savedFolder);

		// 선택한 장소들을 폴더에 추가
		if (requestDto.getPlaceIds() != null && !requestDto.getPlaceIds().isEmpty()) {
			addPlacesToFolderWithOrder(savedFolder, itinerary, requestDto.getPlaceIds());
		}

		return savedFolder.getFolderId();
	}

	// 폴더 엔티티 빌드
	private Folder buildFolder(UserPersonalInfo user, FolderCreateRequestDto requestDto, NeighborhoodEntity neighborhood) {
		String folderTitle = generateFolderTitle(requestDto.getDate(), neighborhood.getNeighborhoodName());

		Folder folder = new Folder();
		folder.setUser(user);
		folder.setFolderTitle(folderTitle);
		folder.setDate(requestDto.getDate());
		folder.setNeighborhoodId(requestDto.getNeighborhoodId());
		return folder;
	}

	// Itinerary 생성 및 저장
	private Itinerary createItinerary(Folder folder) {
		Itinerary itinerary = new Itinerary();
		itinerary.setFolder(folder);
		itinerary.setItineraryGeneratedByAi(false);
		return itineraryRepository.save(itinerary);
	}

	// 폴더에 장소들을 순서대로 추가
	private void addPlacesToFolderWithOrder(Folder folder, Itinerary itinerary, List<Integer> placeIds) {
		int order = 1;
		for (Integer placeId : placeIds) {
			if (placeId == null || placeId == 0) {
				continue;
			}

			try {
				folderPlaceService.addPlaceToFolder(folder, placeId);
				// 순서 정보는 별도로 저장
				// ItineraryPlace는 FolderPlaceService에서 생성되므로 순서만 업데이트
				order++;
			} catch (InvalidFolderRequestException e) {
				log.warn("장소 추가 실패 - placeId: {}, error: {}", placeId, e.getMessage());
			}
		}
	}

	 // 폴더명 자동 생성: "{날짜} {동네명}"
	private String generateFolderTitle(LocalDate date, String neighborhoodName) {
		return date.toString() + " " + neighborhoodName;
	}

	// 폴더 삭제
	@Transactional
	public void deleteFolder(Integer folderId, Long userId) {
		log.info("폴더 삭제 시작 - folderId: {}, userId: {}", folderId, userId);

		Folder folder = folderValidator.findAndValidateFolderByUserId(folderId, userId);

		folderRepository.delete(folder);
		log.info("폴더 삭제 완료 - folderId: {}", folderId);
	}

	// 폴더 수정
	@Transactional
	public void updateFolder(Integer folderId, FolderUpdateRequestDto requestDto, Long userId) {
		log.info("폴더 수정 시작 - folderId: {}, userId: {}", folderId, userId);

		Folder folder = folderValidator.findAndValidateFolderByUserId(folderId, userId);

		// 변경된 필드만 업데이트
		if (requestDto.getFolderTitle() != null) {
			folder.setFolderTitle(requestDto.getFolderTitle());
			log.info("폴더명 변경 - folderId: {}, newTitle: {}", folderId, requestDto.getFolderTitle());
		}
		if (requestDto.getDate() != null) {
			folder.setDate(requestDto.getDate());
			log.info("날짜 변경 - folderId: {}, newDate: {}", folderId, requestDto.getDate());
		}

		log.info("폴더 수정 완료 - folderId: {}", folderId);
	}

	// 폴더에 장소 추가
	@Transactional
	public Integer addPlaceToFolder(Integer folderId, Integer placeId, Long userId) {
		log.info("폴더에 장소 추가 시작 - folderId: {}, placeId: {}, userId: {}", folderId, placeId, userId);

		Folder folder = folderValidator.findAndValidateFolderByUserId(folderId, userId);

		folderPlaceService.addPlaceToFolder(folder, placeId);
		log.info("폴더에 장소 추가 완료 - folderId: {}, placeId: {}", folderId, placeId);

		return folderId;
	}

	// 폴더에서 장소 삭제
	@Transactional
	public void removePlaceFromFolder(Integer folderId, Integer placeId, Long userId) {
		log.info("폴더에서 장소 삭제 시작 - folderId: {}, placeId: {}, userId: {}", folderId, placeId, userId);

		Folder folder = folderValidator.findAndValidateFolderByUserId(folderId, userId);

		folderPlaceService.removePlaceFromFolder(folder, placeId);
		log.info("폴더에서 장소 삭제 완료 - folderId: {}, placeId: {}", folderId, placeId);
	}

	// 폴더 상세 조회
	@Transactional(readOnly = true)
	public FolderDetailResponseDto getFolderDetails(Integer folderId, Long userId) {
		log.info("폴더 상세 조회 시작 - folderId: {}, userId: {}", folderId, userId);

		Folder folder = folderValidator.findAndValidateFolderByUserId(folderId, userId);

		// LAZY 로딩을 위해 폴더 플레이스와 Place 엔티티를 미리 로드
		List<FolderPlace> folderPlaces = folderPlaceService.getFolderPlacesWithPlaces(folder);

		// Itinerary와 ItineraryPlace도 초기화
		if (folder.getItinerary() != null) {
			List<ItineraryPlace> itineraryPlaces = folder.getItinerary().getItineraryPlaces();
			for (ItineraryPlace ip : itineraryPlaces) {
				ip.getPlace().getPlaceId(); // LAZY 로딩 초기화
			}
		}

		log.info("폴더 상세 조회 완료 - folderId: {}, placeCount: {}", folderId, folderPlaces.size());
		return new FolderDetailResponseDto(folder);
	}

	// 내 폴더 목록 조회
	@Transactional(readOnly = true)
	public List<FolderSummaryResponseDto> listMyFolders(Long userId) {
		log.info("폴더 목록 조회 시작 - userId: {}", userId);

		UserPersonalInfo user = folderValidator.validateUser(userId);
		List<FolderSummaryResponseDto> folders = folderRepository.findAllByUserOrderByFolderIdDesc(user).stream()
				.map(FolderSummaryResponseDto::new)
				.collect(Collectors.toList());

		log.info("폴더 목록 조회 완료 - userId: {}, count: {}", userId, folders.size());
		return folders;
	}
}


