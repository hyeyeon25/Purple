package Purple.Purple.folder.controller;

import Purple.Purple.folder.dto.FolderCreateRequestDto;
import Purple.Purple.folder.dto.FolderDetailResponseDto;
import Purple.Purple.folder.dto.FolderSummaryResponseDto;
import Purple.Purple.folder.dto.FolderUpdateRequestDto;
import Purple.Purple.itinerery.dto.PlaceAddRequestDto;
import Purple.Purple.itinerery.dto.RouteUpdateRequestDto;
import Purple.Purple.folder.service.FolderService;
import Purple.Purple.itinerery.service.RouteService;
import Purple.Purple.user.entity.UserPersonalInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/folders")
public class FolderController {

	private final FolderService folderService;
	private final RouteService routeService;

	// 폴더 생성 (Itinerary 대리)
	@PostMapping
	public ResponseEntity<Integer> createFolder(@RequestBody FolderCreateRequestDto requestDto,
	                                            @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
		Long currentUserId = userPersonalInfo.getUserId();
		Integer id = folderService.createFolder(requestDto, currentUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	// 내 폴더 목록
	@GetMapping
	public ResponseEntity<List<FolderSummaryResponseDto>> listMyFolders(@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
		Long currentUserId = userPersonalInfo.getUserId();
		List<FolderSummaryResponseDto> list = folderService.listMyFolders(currentUserId);
		return ResponseEntity.ok(list);
	}

	// 폴더 상세
	@GetMapping("/{folderId}")
	public ResponseEntity<FolderDetailResponseDto> getFolder(@PathVariable Integer folderId,
	                                                            @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
		Long currentUserId = userPersonalInfo.getUserId();
		FolderDetailResponseDto dto = folderService.getFolderDetails(folderId, currentUserId);
		return ResponseEntity.ok(dto);
	}

	// 폴더 수정
	@PutMapping("/{folderId}")
	public ResponseEntity<Void> updateFolder(@PathVariable Integer folderId,
	                                         @RequestBody FolderUpdateRequestDto requestDto,
	                                         @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
		Long currentUserId = userPersonalInfo.getUserId();
		folderService.updateFolder(folderId, requestDto, currentUserId);
		return ResponseEntity.noContent().build();
	}

	// 폴더 삭제
	@DeleteMapping("/{folderId}")
	public ResponseEntity<Void> deleteFolder(@PathVariable Integer folderId,
	                                         @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
		Long currentUserId = userPersonalInfo.getUserId();
		folderService.deleteFolder(folderId, currentUserId);
		return ResponseEntity.noContent().build();
	}

	// 폴더에 장소 추가
	@PostMapping("/{folderId}/places")
	public ResponseEntity<Integer> addPlaceToFolder(@PathVariable Integer folderId,
	                                                @RequestBody PlaceAddRequestDto requestDto,
	                                                @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
		Long currentUserId = userPersonalInfo.getUserId();
		Integer resultFolderId = folderService.addPlaceToFolder(folderId, requestDto.getPlaceId(), currentUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(resultFolderId);
	}

	// 폴더의 경로(방문 순서) 설정 - 각 폴더 당 한 개의 경로만 유지
	@PutMapping("/{folderId}/route")
	public ResponseEntity<Void> updateFolderRoute(@PathVariable Integer folderId,
	                                              @RequestBody RouteUpdateRequestDto requestDto,
	                                              @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
		Long currentUserId = userPersonalInfo.getUserId();
		routeService.putRoute(folderId, requestDto, currentUserId);
		return ResponseEntity.noContent().build();
	}

	// 폴더의 경로(방문 순서) 조회
	@GetMapping("/{folderId}/route")
	public ResponseEntity<List<Integer>> getFolderRoute(@PathVariable Integer folderId,
	                                                    @AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {
		Long currentUserId = userPersonalInfo.getUserId();
		List<Integer> route = routeService.getRoute(folderId, currentUserId);
		return ResponseEntity.ok(route);
	}
}


