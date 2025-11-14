package Purple.Purple.Neighborhood.controller;

import Purple.Purple.Neighborhood.dto.NeighborhoodRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.PlaceRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.UserPreferenceRequestDto;
import Purple.Purple.Neighborhood.service.NeighborhoodRecommendationService;

// --- Swagger 어노테이션 임포트 ---
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
// --- (임포트 끝) ---

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 동네 관련 API 컨트롤러
 */
@Tag(name = "동네 추천 API", description = "사용자 선호도(태그) 기반 동네 및 장소 추천 API") //  API 그룹 설정
@RestController
@RequestMapping("/api/v1/neighborhoods")
@RequiredArgsConstructor
@Slf4j
public class NeighborhoodController {

    private final NeighborhoodRecommendationService recommendationService;

    /**
     * 사용자 ID 기반 동네 Top 3 추천 API
     * 사용자의 저장된 선호도 벡터를 자동으로 조회하여 추천합니다.
     */
    @Operation(summary = "동네 Top 3 추천", description = "사용자 ID를 기반으로 저장된 선호도 벡터를 자동으로 조회하여 가장 유사도가 높은 동네 Top 3를 추천합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동네 추천 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NeighborhoodRecommendationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자의 선호도 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/recommend/{userId}")
    public ResponseEntity<List<NeighborhoodRecommendationResponseDto>> recommendNeighborhoods(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long userId) {

        log.info("GET /api/v1/neighborhoods/recommend/{} - userId: {}", userId, userId);

        List<NeighborhoodRecommendationResponseDto> recommendations =
                recommendationService.recommendTop3NeighborhoodsByUserId(userId);

        log.info("Successfully recommended {} neighborhoods for userId: {}", recommendations.size(), userId);

        return ResponseEntity.ok(recommendations);
    }

    /**
     * 사용자 ID 기반 특정 동네 내 장소 추천 API (카테고리 필터링)
     * 사용자의 저장된 선호도 벡터를 자동으로 조회하여 추천합니다.
     */
    @Operation(summary = "특정 동네 내 장소 추천", description = "사용자 ID를 기반으로 저장된 선호도 벡터를 자동으로 조회하여, 특정 동네 내에서 가장 유사한 장소 목록을 추천합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장소 추천 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PlaceRecommendationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "동네를 찾을 수 없거나 사용자의 선호도 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{neighborhoodId}/places/recommend/{userId}")
    public ResponseEntity<List<PlaceRecommendationResponseDto>> recommendPlacesInNeighborhood(
            @Parameter(description = "조회할 동네의 ID", required = true, example = "1")
            @PathVariable Integer neighborhoodId,

            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(description = "필터링할 카테고리", required = false, example = "카페")
            @RequestParam(required = false) String category) {

        log.info("GET /api/v1/neighborhoods/{}/places/recommend/{} - Category: {}, userId: {}",
                neighborhoodId, userId, category, userId);

        List<PlaceRecommendationResponseDto> recommendations =
                recommendationService.recommendPlacesInNeighborhoodByUserId(neighborhoodId, category, userId);

        log.info("Successfully recommended {} places in neighborhood ID: {} for userId: {} (category: {})",
                recommendations.size(), neighborhoodId, userId, category);

        return ResponseEntity.ok(recommendations);
    }
}