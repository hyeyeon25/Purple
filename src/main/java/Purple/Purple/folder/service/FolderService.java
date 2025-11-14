package Purple.Purple.folder.service;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.folder.domain.FolderPlace;
import Purple.Purple.folder.repository.FolderPlaceRepository;
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
import Purple.Purple.itinerery.dto.PlaceAddRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

	@Transactional
	public Integer createFolder(FolderCreateRequestDto requestDto, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		
		NeighborhoodEntity neighborhood = neighborhoodRepository.findById(requestDto.getNeighborhoodId())
				.orElseThrow(() -> new IllegalArgumentException("해당 동네를 찾을 수 없습니다. id=" + requestDto.getNeighborhoodId()));
		
		String folderTitle = requestDto.getDate().toString() + " " + neighborhood.getNeighborhoodName();
		
		Folder folder = new Folder();
		folder.setUser(user);
		folder.setFolderTitle(folderTitle);
		folder.setDate(requestDto.getDate());
		Folder saved = folderRepository.save(folder);
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
	public Integer addPlaceToFolder(Integer folderId, PlaceAddRequestDto requestDto, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));
		
		PlaceEntity place;
		
		// placeId가 있으면 기존 장소 사용
		if (requestDto.getPlaceId() != null) {
			place = placeRepository.findById(requestDto.getPlaceId())
					.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + requestDto.getPlaceId()));
		} else {
			// placeId가 없고 kakaoPlaceId가 있으면 중복 체크 후 생성
			if (requestDto.getKakaoPlaceId() == null) {
				throw new IllegalArgumentException("placeId 또는 kakaoPlaceId 중 하나는 필수입니다.");
			}
			
			// 중복 체크
			place = placeRepository.findByKakaoPlaceId(requestDto.getKakaoPlaceId())
					.orElseGet(() -> {
						// 장소가 없으면 생성
						NeighborhoodEntity neighborhood = neighborhoodRepository.findById(requestDto.getNeighborhoodId())
								.orElseThrow(() -> new IllegalArgumentException("해당 동네를 찾을 수 없습니다. id=" + requestDto.getNeighborhoodId()));
						
						PlaceEntity newPlace = new PlaceEntity();
						newPlace.setKakaoPlaceId(requestDto.getKakaoPlaceId());
						newPlace.setPlaceName(requestDto.getPlaceName());
						newPlace.setPlaceCategory(requestDto.getPlaceCategory());
						newPlace.setAddress(requestDto.getAddress());
						newPlace.setLatitude(requestDto.getLatitude());
						newPlace.setLongitude(requestDto.getLongitude());
						newPlace.setNeighborhood(neighborhood);
						newPlace.setIsIndoor(true); // 기본값
						newPlace.setStayDurationMinutes(60); // 기본값
						
						return placeRepository.save(newPlace);
					});
		}
		
		// 폴더에 이미 담겨 있으면 중복 추가 방지
		if (folderPlaceRepository.existsByFolderAndPlace(folder, place)) {
			return folderId; // 이미 담겨 있음
		}
		
		FolderPlace fp = new FolderPlace();
		fp.setFolder(folder);
		fp.setPlace(place);
		folderPlaceRepository.save(fp);
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
		folderPlaceRepository.delete(fp);
	}

	@Transactional
	public FolderDetailResponseDto getFolderDetails(Integer folderId, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));
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
}


