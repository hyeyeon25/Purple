package Purple.Purple.Neighborhood.controller;

import Purple.Purple.Neighborhood.dto.NeighborhoodRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.PagedPlaceRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.PlaceRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.UserPreferenceRequestDto;
import Purple.Purple.Neighborhood.service.NeighborhoodRecommendationService;

// --- Swagger 어노테이션 임포트 ---
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

@Tag(name = "동네 추천 API", description = "사용자 선호도(태그) 기반 동네 및 장소 추천 API")
@RestController
@RequestMapping("/api/v1/neighborhoods")
@RequiredArgsConstructor
@Slf4j
public class NeighborhoodController {

    private final NeighborhoodRecommendationService recommendationService;

    /**
     * 사용자 ID 기반 동네 Top 3 추천 API
     */
    @Operation(
            summary = "동네 Top 3 추천",
            description = "사용자 ID를 기반으로 저장된 선호도 벡터를 자동으로 조회하여 가장 유사도가 높은 동네 Top 3를 추천합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "동네 추천 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = NeighborhoodRecommendationResponseDto.class)
                            ),
                            examples = @ExampleObject(
                                    value = "[\n" +
                                            "  {\n" +
                                            "    \"neighborhoodId\": 23,\n" +
                                            "    \"neighborhoodName\": \"백석동\",\n" +
                                            "    \"averageSimilarityScore\": 0.335337859007003,\n" +
                                            "    \"placeCount\": 77,\n" +
                                            "    \"rank\": 1\n" +
                                            "  },\n" +
                                            "  {\n" +
                                            "    \"neighborhoodId\": 27,\n" +
                                            "    \"neighborhoodName\": \"입장면\",\n" +
                                            "    \"averageSimilarityScore\": 0.3316658496452047,\n" +
                                            "    \"placeCount\": 34,\n" +
                                            "    \"rank\": 2\n" +
                                            "  },\n" +
                                            "  {\n" +
                                            "    \"neighborhoodId\": 7,\n" +
                                            "    \"neighborhoodName\": \"원성동\",\n" +
                                            "    \"averageSimilarityScore\": 0.32429815794365197,\n" +
                                            "    \"placeCount\": 63,\n" +
                                            "    \"rank\": 3\n" +
                                            "  }\n" +
                                            "]"
                            )
                    )
            ),
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
     * 사용자 ID 기반 특정 동네 내 장소 추천 API (카테고리 필터링, 페이지네이션)
     */
    @Operation(summary = "특정 동네 내 장소 추천 (페이지네이션)", description = "사용자 ID를 기반으로 저장된 선호도 벡터를 자동으로 조회하여, 특정 동네 내에서 가장 유사한 장소 목록을 페이지 단위로 추천합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장소 추천 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedPlaceRecommendationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "동네를 찾을 수 없거나 사용자의 선호도 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{neighborhoodId}/places/recommend/{userId}")
    public ResponseEntity<PagedPlaceRecommendationResponseDto> recommendPlacesInNeighborhood(
            @Parameter(description = "조회할 동네의 ID", required = true, example = "1")
            @PathVariable Integer neighborhoodId,

            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long userId,

            @Parameter(description = "필터링할 카테고리", required = false, example = "카페")
            @RequestParam(required = false) String category,

            @Parameter(description = "페이지 번호 (0부터 시작)", required = false, example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", required = false, example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/v1/neighborhoods/{}/places/recommend/{} - Category: {}, Page: {}, Size: {}, userId: {}",
                neighborhoodId, userId, category, page, size, userId);

        PagedPlaceRecommendationResponseDto recommendations =
                recommendationService.recommendPlacesInNeighborhoodByUserId(neighborhoodId, category, userId, page, size);

        log.info("Successfully recommended {} places in neighborhood ID: {} for userId: {} (category: {}, page: {}/{})",
                recommendations.getContent().size(), neighborhoodId, userId, category, page + 1, recommendations.getTotalPages());

        return ResponseEntity.ok(recommendations);
    }
}