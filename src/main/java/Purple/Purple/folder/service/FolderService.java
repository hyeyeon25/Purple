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
		
		// 필수 필드 검증
		if (requestDto.getDate() == null) {
			throw new IllegalArgumentException("여행 날짜는 필수입니다.");
		}
		if (requestDto.getNeighborhoodId() == null) {
			throw new IllegalArgumentException("동네 ID는 필수입니다.");
		}
		
		// 동네 정보 조회
		NeighborhoodEntity neighborhood = neighborhoodRepository.findById(requestDto.getNeighborhoodId())
				.orElseThrow(() -> new IllegalArgumentException("해당 동네를 찾을 수 없습니다. id=" + requestDto.getNeighborhoodId()));
		
		// 폴더명 자동 생성: "{여행 날짜} + {여행 동네 이름}"
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
	public Integer addPlaceToFolder(Integer folderId, Integer placeId, Long userId) {
		UserPersonalInfo user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new IllegalArgumentException("해당 폴더를 찾을 수 없거나 소유자가 아닙니다. id=" + folderId));
		PlaceEntity place = placeRepository.findById(placeId)
				.orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + placeId));
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


