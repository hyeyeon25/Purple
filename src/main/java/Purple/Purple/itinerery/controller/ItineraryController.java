package Purple.Purple.itinerery.controller;

import Purple.Purple.folder.dto.FolderPlaceResponseDto;
import Purple.Purple.itinerery.dto.ItineraryUpdateRequestDto;
import Purple.Purple.itinerery.service.ItineraryService;
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
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/itinerary")
@Tag(name = "Itinerary API", description = "여행 일정 관리 API (일정 조회, 일정 설정)")
public class ItineraryController {

	private final ItineraryService itineraryService;

	@Operation(summary = "일정 조회", description = "폴더 ID를 이용해 일정(방문 순서)을 조회합니다. 일정이 없으면 자동으로 생성합니다. develop 브랜치의 itinerary DTO 형식에 맞춰 상세 정보를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
			@ApiResponse(responseCode = "404", description = "폴더를 찾을 수 없음")
	})
	@GetMapping("/{folderId}")
	public ResponseEntity<List<FolderPlaceResponseDto>> getItinerary(
			@PathVariable Integer folderId,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			Long currentUserId = userPersonalInfo.getUserId();
			log.info("일정 조회 요청 - folderId: {}, userId: {}", folderId, currentUserId);
			List<FolderPlaceResponseDto> itinerary = itineraryService.getRouteDetails(folderId, currentUserId);
			log.info("일정 조회 성공 - folderId: {}, itinerary size: {}", folderId, itinerary.size());
			return ResponseEntity.ok(itinerary);
		} catch (IllegalArgumentException e) {
			// 폴더를 찾을 수 없는 경우 404, 그 외는 400
			if (e.getMessage() != null && e.getMessage().contains("폴더를 찾을 수 없")) {
				log.error("일정 조회 실패 - 폴더를 찾을 수 없음: folderId: {}, error: {}", folderId, e.getMessage());
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			log.error("일정 조회 실패 - folderId: {}, error: {}", folderId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception e) {
			log.error("일정 조회 중 예외 발생 - folderId: {}", folderId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(summary = "일정 순서 변경", description = "사용자가 드래그로 변경한 장소들의 방문 순서를 저장합니다. placeIds는 방문 순서대로 정렬된 장소 ID 리스트입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "순서 변경 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 (placeIds가 없거나 폴더에 없는 장소 포함)"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
			@ApiResponse(responseCode = "404", description = "폴더를 찾을 수 없음")
	})
	@PutMapping("/{folderId}")
	public ResponseEntity<Void> updateItinerary(
			@PathVariable Integer folderId,
			@RequestBody ItineraryUpdateRequestDto requestDto,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		if (requestDto == null || requestDto.getPlaceIds() == null || requestDto.getPlaceIds().isEmpty()) {
			log.error("일정 순서 변경 실패 - placeIds가 없음: folderId: {}", folderId);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		try {
			Long currentUserId = userPersonalInfo.getUserId();
			log.info("일정 순서 변경 요청 - folderId: {}, userId: {}, placeIds: {}", 
					folderId, currentUserId, requestDto.getPlaceIds());
			itineraryService.updateItineraryOrder(folderId, requestDto.getPlaceIds(), currentUserId);
			log.info("일정 순서 변경 성공 - folderId: {}", folderId);
			return ResponseEntity.noContent().build();
		} catch (IllegalArgumentException e) {
			// 폴더를 찾을 수 없는 경우 404, 그 외는 400
			if (e.getMessage() != null && e.getMessage().contains("폴더를 찾을 수 없")) {
				log.error("일정 순서 변경 실패 - 폴더를 찾을 수 없음: folderId: {}, error: {}", folderId, e.getMessage());
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			log.error("일정 순서 변경 실패 - folderId: {}, error: {}", folderId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception e) {
			log.error("일정 순서 변경 중 예외 발생 - folderId: {}", folderId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(summary = "경로 추천 재생성", description = "기존 경로를 무시하고 거리 기반 최적 경로를 새로 생성합니다. 버튼 클릭 시 호출하여 경로를 다시 추천받을 수 있습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "경로 추천 재생성 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
			@ApiResponse(responseCode = "404", description = "폴더를 찾을 수 없음")
	})
	@PostMapping("/{folderId}/recommend")
	public ResponseEntity<?> regenerateRecommendedRoute(
			@PathVariable Integer folderId,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		log.info("경로 추천 재생성 요청 시작 - folderId: {}, userPersonalInfo: {}", folderId, userPersonalInfo != null ? "존재" : "null");

		if (userPersonalInfo == null) {
			log.warn("경로 추천 재생성 실패 - 인증되지 않은 사용자: folderId: {}", folderId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "인증되지 않은 사용자입니다."));
		}

		try {
			Long currentUserId = userPersonalInfo.getUserId();
			log.info("경로 추천 재생성 요청 - folderId: {}, userId: {}", folderId, currentUserId);
			
			List<FolderPlaceResponseDto> recommendedRoute = itineraryService.regenerateRecommendedRoute(folderId, currentUserId);
			
			log.info("경로 추천 재생성 성공 - folderId: {}, route size: {}", folderId, recommendedRoute.size());
			return ResponseEntity.ok(recommendedRoute);
		} catch (IllegalArgumentException e) {
			log.error("경로 추천 재생성 실패 - IllegalArgumentException: folderId: {}, error: {}", folderId, e.getMessage(), e);
			
			// 폴더를 찾을 수 없는 경우 404, 그 외는 400
			if (e.getMessage() != null && e.getMessage().contains("폴더를 찾을 수 없")) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("error", e.getMessage()));
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다."));
		} catch (Exception e) {
			log.error("경로 추천 재생성 중 예외 발생 - folderId: {}, exception: {}", folderId, e.getClass().getName(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "서버 내부 오류가 발생했습니다: " + e.getMessage()));
		}
	}
}

