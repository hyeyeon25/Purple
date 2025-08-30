package Purple.Purple.places.controller;

import Purple.Purple.places.dto.PlacesResponseDto;
import Purple.Purple.places.service.PlacesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PlacesController {

    private final PlacesService placesService;

    @Operation(
            summary = "동네 내 추천 장소 리스트 조회",
            description = "선택된 동네 내에서 사용자 성향 기반 장소를 조회합니다. 카테고리/검색/정렬/페이지네이션/거리 계산을 지원합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 장소 리스트"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/places/{neighborhoodId}")
    public ResponseEntity<PlacesResponseDto> getPlacesInNeighborhood(
            @Parameter(description = "동네 ID") @PathVariable int neighborhoodId,
            @Parameter(description = "카테고리 필터") @RequestParam(required = false) String category,
            @Parameter(description = "검색어(가게명/주소/태그)") @RequestParam(required = false) String q,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "match") String sort,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "거리 정렬/계산 기준 위도") @RequestParam(required = false) Float centerLat,
            @Parameter(description = "거리 정렬/계산 기준 경도") @RequestParam(required = false) Float centerLng
    ) {
        PlacesResponseDto response = placesService.getPlacesInNeighborhood(
                neighborhoodId, category, q, sort, page, size, centerLat, centerLng
        );
        return ResponseEntity.ok(response);
    }

//    @Operation(
//            summary = "장소 상세 정보 조회",
//            description = "카드/마커 클릭 시 노출되는 상세. 주소, 영업시간, 실내 여부, 리뷰/평점 등을 반환합니다.",
//            security = @SecurityRequirement(name = "bearerAuth")
//    )
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "장소 상세 정보"),
//            @ApiResponse(responseCode = "404", description = "없음")
//    })
//    @GetMapping("/locations/{locationId}")
//    public ResponseEntity<LocationDetailDto> getLocationDetails(
//            @Parameter(description = "장소 ID") @PathVariable int locationId
//    ) {
//        LocationDetailDto response = placesService.getLocationDetails(locationId);
//        return ResponseEntity.ok(response);
//    }
}