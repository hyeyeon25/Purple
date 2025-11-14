package Purple.Purple.folder.controller;

import Purple.Purple.folder.dto.*;
import Purple.Purple.itinerery.dto.PlaceAddRequestDto;
import Purple.Purple.itinerery.dto.RouteUpdateRequestDto;
import Purple.Purple.folder.service.FolderService;
import Purple.Purple.itinerery.service.RouteService;
import Purple.Purple.user.entity.UserPersonalInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/folders")
@Tag(name = "Folder API", description = "여행 폴더 관리 (CRUD, 장소 추가, 경로 설정/조회)")
public class FolderController {

	private final FolderService folderService;
	private final RouteService routeService;

	@Operation(summary = "폴더 생성", description = "새 여행 폴더를 생성합니다. (Itinerary 대리 역할)")
	@ApiResponse(responseCode = "201", description = "폴더 생성 성공")
	@PostMapping
	public ResponseEntity<Integer> createFolder(
			@RequestBody FolderCreateRequestDto requestDto,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		Long currentUserId = userPersonalInfo.getUserId();
		Integer id = folderService.createFolder(requestDto, currentUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
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
	@GetMapping("/{folderId}")
	public ResponseEntity<FolderDetailResponseDto> getFolder(
			@PathVariable Integer folderId,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		Long currentUserId = userPersonalInfo.getUserId();
		FolderDetailResponseDto dto = folderService.getFolderDetails(folderId, currentUserId);
		return ResponseEntity.ok(dto);
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

	@Operation(summary = "폴더에 장소 추가", description = "지정한 폴더에 장소를 추가합니다.")
	@ApiResponse(responseCode = "201", description = "장소 추가 성공")
	@PostMapping("/{folderId}/places")
	public ResponseEntity<Integer> addPlaceToFolder(
			@PathVariable Integer folderId,
			@RequestBody PlaceAddRequestDto requestDto,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		Long currentUserId = userPersonalInfo.getUserId();
		Integer resultFolderId = folderService.addPlaceToFolder(folderId, requestDto, currentUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(resultFolderId);
	}

	@Operation(summary = "폴더 경로 설정", description = "폴더 내 장소들의 방문 순서를 설정합니다. placeIdsInOrder가 없으면 폴더에 있는 모든 장소로 자동 경로를 생성합니다. (각 폴더 당 1개의 경로 유지)")
	@ApiResponse(responseCode = "204", description = "경로 설정 성공")
	@PutMapping("/{folderId}/route")
	public ResponseEntity<Void> updateFolderRoute(
			@PathVariable Integer folderId,
			@RequestBody(required = false) RouteUpdateRequestDto requestDto,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			Long currentUserId = userPersonalInfo.getUserId();
			// requestDto가 null이면 빈 DTO 생성 (자동 경로 생성)
			if (requestDto == null) {
				requestDto = new RouteUpdateRequestDto();
			}
			routeService.putRoute(folderId, requestDto, currentUserId);
			return ResponseEntity.noContent().build();
		} catch (IllegalArgumentException e) {
			// 비즈니스 로직 예외는 400 Bad Request로 반환 (에러 메시지 포함)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(null); // 204와 달리 body는 null이지만 상태 코드로 구분
		} catch (Exception e) {
			// 기타 예외는 500 Internal Server Error로 반환
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(summary = "폴더 경로 조회", description = "폴더 내 장소들의 현재 방문 순서를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/{folderId}/route")
	public ResponseEntity<List<Integer>> getFolderRoute(
			@PathVariable Integer folderId,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		Long currentUserId = userPersonalInfo.getUserId();
		List<Integer> route = routeService.getRoute(folderId, currentUserId);
		return ResponseEntity.ok(route);
	}
}
