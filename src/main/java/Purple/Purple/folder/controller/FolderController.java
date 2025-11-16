package Purple.Purple.folder.controller;

import Purple.Purple.folder.dto.*;
import Purple.Purple.folder.service.FolderService;
import Purple.Purple.user.entity.UserPersonalInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/folders")
@Tag(name = "Folder API", description = "여행 폴더 관리 (CRUD, 장소 추가)")
public class FolderController {

	private final FolderService folderService;

	@Operation(summary = "폴더 생성", description = "새 여행 폴더를 생성합니다. 폴더 이름과 선택한 장소들을 함께 지정할 수 있습니다.")
	@ApiResponse(responseCode = "201", description = "폴더 생성 성공")
	@PostMapping
	public ResponseEntity<Integer> createFolder(
			@RequestBody FolderCreateRequestDto requestDto,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			Long currentUserId = userPersonalInfo.getUserId();
			Integer id = folderService.createFolder(requestDto, currentUserId);
			return ResponseEntity.status(HttpStatus.CREATED).body(id);
		} catch (IllegalArgumentException e) {
			// 비즈니스 로직 예외는 400 Bad Request로 반환
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception e) {
			// 기타 예외는 500 Internal Server Error로 반환
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(summary = "내 폴더 목록 조회", description = "현재 로그인한 사용자의 모든 폴더 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping
	public ResponseEntity<List<FolderSummaryResponseDto>> listMyFolders(
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		Long currentUserId = userPersonalInfo.getUserId();
		List<FolderSummaryResponseDto> list = folderService.listMyFolders(currentUserId);
		return ResponseEntity.ok(list);
	}

	@Operation(summary = "폴더 상세 조회", description = "폴더 ID를 이용해 상세 정보를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "폴더를 찾을 수 없음")
	@GetMapping("/{folderId}")
	public ResponseEntity<FolderDetailResponseDto> getFolder(
			@PathVariable Integer folderId,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			Long currentUserId = userPersonalInfo.getUserId();
			FolderDetailResponseDto dto = folderService.getFolderDetails(folderId, currentUserId);
			return ResponseEntity.ok(dto);
		} catch (IllegalArgumentException e) {
			log.error("폴더 상세 조회 실패 - folderId: {}, userId: {}, error: {}", 
					folderId, userPersonalInfo != null ? userPersonalInfo.getUserId() : "null", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		} catch (Exception e) {
			log.error("폴더 상세 조회 중 예외 발생 - folderId: {}, userId: {}", 
					folderId, userPersonalInfo != null ? userPersonalInfo.getUserId() : "null", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(summary = "폴더 수정", description = "폴더 이름, 날짜 등 폴더 정보를 수정합니다.")
	@ApiResponse(responseCode = "204", description = "수정 성공 (내용 없음)")
	@PutMapping("/{folderId}")
	public ResponseEntity<Void> updateFolder(
			@PathVariable Integer folderId,
			@RequestBody FolderUpdateRequestDto requestDto,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		Long currentUserId = userPersonalInfo.getUserId();
		folderService.updateFolder(folderId, requestDto, currentUserId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "폴더 삭제", description = "폴더 ID를 이용해 해당 폴더를 삭제합니다.")
	@ApiResponse(responseCode = "204", description = "삭제 성공 (내용 없음)")
	@DeleteMapping("/{folderId}")
	public ResponseEntity<Void> deleteFolder(
			@PathVariable Integer folderId,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		Long currentUserId = userPersonalInfo.getUserId();
		folderService.deleteFolder(folderId, currentUserId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "폴더에 장소 추가", description = "지정한 폴더에 장소를 추가합니다. placeId만 받아서 DB에서 장소 정보를 조회합니다.")
	@ApiResponse(responseCode = "201", description = "장소 추가 성공")
	@PostMapping("/{folderId}/places")
	public ResponseEntity<Integer> addPlaceToFolder(
			@PathVariable Integer folderId,
			@RequestBody PlaceIdRequestDto requestDto,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		if (requestDto.getPlaceId() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		Long currentUserId = userPersonalInfo.getUserId();
		Integer resultFolderId = folderService.addPlaceToFolder(folderId, requestDto.getPlaceId(), currentUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(resultFolderId);
	}

	@Operation(summary = "폴더에서 장소 삭제", description = "지정한 폴더에서 장소를 삭제합니다. 폴더에서 삭제하면 경로(itinerary)에서도 자동으로 제거됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "장소 삭제 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
			@ApiResponse(responseCode = "404", description = "폴더 또는 장소를 찾을 수 없음")
	})
	@DeleteMapping("/{folderId}/places/{placeId}")
	public ResponseEntity<Void> removePlaceFromFolder(
			@PathVariable Integer folderId,
			@PathVariable Integer placeId,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			Long currentUserId = userPersonalInfo.getUserId();
			folderService.removePlaceFromFolder(folderId, placeId, currentUserId);
			return ResponseEntity.noContent().build();
		} catch (IllegalArgumentException e) {
			log.error("폴더에서 장소 삭제 실패 - folderId: {}, placeId: {}, error: {}", folderId, placeId, e.getMessage());
			if (e.getMessage() != null && e.getMessage().contains("찾을 수 없")) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception e) {
			log.error("폴더에서 장소 삭제 중 예외 발생 - folderId: {}, placeId: {}", folderId, placeId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
