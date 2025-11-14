package Purple.Purple.route.controller;

import Purple.Purple.itinerery.dto.RouteUpdateRequestDto;
import Purple.Purple.itinerery.service.RouteService;
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
@RequestMapping("/api/v1/route")
@Tag(name = "Route API", description = "경로 관리 API (경로 조회, 경로 설정)")
public class RouteController {

	private final RouteService routeService;

	@Operation(summary = "경로 조회", description = "폴더 ID를 이용해 경로(방문 순서)를 조회합니다. 경로가 없으면 자동으로 생성합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
			@ApiResponse(responseCode = "404", description = "폴더를 찾을 수 없음")
	})
	@GetMapping("/{folderId}")
	public ResponseEntity<List<Integer>> getRoute(
			@PathVariable Integer folderId,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			Long currentUserId = userPersonalInfo.getUserId();
			log.info("경로 조회 요청 - folderId: {}, userId: {}", folderId, currentUserId);
			List<Integer> route = routeService.getRoute(folderId, currentUserId);
			log.info("경로 조회 성공 - folderId: {}, route size: {}", folderId, route.size());
			return ResponseEntity.ok(route);
		} catch (IllegalArgumentException e) {
			log.error("경로 조회 실패 - folderId: {}, error: {}", folderId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception e) {
			log.error("경로 조회 중 예외 발생 - folderId: {}", folderId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(summary = "경로 설정", description = "폴더 내 장소들의 방문 순서를 설정합니다. placeIdsInOrder가 없으면 폴더에 있는 모든 장소로 자동 경로를 생성합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "경로 설정 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
			@ApiResponse(responseCode = "404", description = "폴더를 찾을 수 없음")
	})
	@PutMapping("/{folderId}")
	public ResponseEntity<Void> updateRoute(
			@PathVariable Integer folderId,
			@RequestBody(required = false) RouteUpdateRequestDto requestDto,
			@AuthenticationPrincipal UserPersonalInfo userPersonalInfo) {

		if (userPersonalInfo == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			Long currentUserId = userPersonalInfo.getUserId();
			log.info("경로 설정 요청 - folderId: {}, userId: {}", folderId, currentUserId);
			// requestDto가 null이면 빈 DTO 생성 (자동 경로 생성)
			if (requestDto == null) {
				requestDto = new RouteUpdateRequestDto();
			}
			routeService.putRoute(folderId, requestDto, currentUserId);
			log.info("경로 설정 성공 - folderId: {}", folderId);
			return ResponseEntity.noContent().build();
		} catch (IllegalArgumentException e) {
			log.error("경로 설정 실패 - folderId: {}, error: {}", folderId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception e) {
			log.error("경로 설정 중 예외 발생 - folderId: {}", folderId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}

