package Purple.Purple.folder.service;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.folder.dto.FolderCreateRequestDto;
import Purple.Purple.folder.exception.FolderNotFoundException;
import Purple.Purple.folder.exception.InvalidFolderRequestException;
import Purple.Purple.folder.repository.FolderRepository;
import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import Purple.Purple.Neighborhood.repository.NeighborhoodRepository;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// 폴더 관련 요청의 유효성 검증 컴포넌트
@Component
@RequiredArgsConstructor
public class FolderValidator {

    private final UserRepository userRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final FolderRepository folderRepository;

    // 사용자 유효성 검증
    public UserPersonalInfo validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InvalidFolderRequestException("해당 사용자를 찾을 수 없습니다. id=" + userId));
    }

    // 폴더 생성 요청 유효성 검증
    public void validateCreateRequest(FolderCreateRequestDto requestDto) {
        validateNeighborhoodId(requestDto.getNeighborhoodId());
        validateDate(requestDto.getDate());
    }

    // 동네 ID 유효성 검증
    public NeighborhoodEntity validateNeighborhoodId(Integer neighborhoodId) {
        if (neighborhoodId == null || neighborhoodId == 0) {
            throw new InvalidFolderRequestException("유효한 동네 ID가 필요합니다. (neighborhoodId: " + neighborhoodId + ")");
        }

        return neighborhoodRepository.findById(neighborhoodId)
                .orElseThrow(() -> new InvalidFolderRequestException("해당 동네를 찾을 수 없습니다. id=" + neighborhoodId));
    }

    // 여행 날짜 유효성 검증
    public void validateDate(LocalDate date) {
        if (date == null) {
            throw new InvalidFolderRequestException("여행 날짜는 필수입니다.");
        }
    }

    // 폴더 소유권 검증
    public void validateFolderOwnership(Folder folder, Long userId) {
        if (!folder.getUser().getUserId().equals(userId)) {
            throw new InvalidFolderRequestException("폴더에 대한 권한이 없습니다. folderId=" + folder.getFolderId());
        }
    }

    // 폴더 조회 및 소유권 검증 (userId로 조회)
    public Folder findAndValidateFolderByUserId(Integer folderId, Long userId) {
        UserPersonalInfo user = validateUser(userId);
        return folderRepository.findByFolderIdAndUser(folderId, user)
                .orElseThrow(() -> new FolderNotFoundException(folderId, userId));
    }

    // 폴더 조회 및 소유권 검증 (UserPersonalInfo로 조회)
    public Folder findAndValidateFolderByUser(Integer folderId, UserPersonalInfo user) {
        return folderRepository.findByFolderIdAndUser(folderId, user)
                .orElseThrow(() -> new FolderNotFoundException(folderId, user.getUserId()));
    }
}

