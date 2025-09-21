package Purple.Purple.place.controller;

import Purple.Purple.place.dto.PlaceResponseDto;
import Purple.Purple.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/places")
@RequiredArgsConstructor
public class PlaceAdminController {

    private final PlaceService placeService;

    @Operation(summary = "천안시 전체 장소 데이터 구축", description = "카카오 API를 반복 호출하여 천안시 전체 동네의 장소 데이터를 DB에 저장합니다. (시간이 매우 오래 걸릴 수 있습니다)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 구축 작업 시작 성공"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생", content = @Content)
    })
    @PostMapping("/fetch-all-cheonan")
    public ResponseEntity<String> fetchAllPlacesForCheonan() {
        placeService.fetchAllPlacesForCheonan();
        return ResponseEntity.ok("천안시 전체 장소 데이터 저장을 시작했습니다. (완료까지 시간이 걸릴 수 있습니다)");
    }

    @Operation(summary = "특정 장소 정보 업데이트", description = "DB에 저장된 특정 장소의 정보를 카카오 API를 통해 최신 정보로 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장소 정보 업데이트 성공",
                    content = @Content(schema = @Schema(implementation = PlaceResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 장소를 찾을 수 없습니다.", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 또는 카카오 API 호출 실패", content = @Content)
    })
    @PostMapping("/update/{placeId}")
    public ResponseEntity<PlaceResponseDto> updatePlace(
            @Parameter(description = "업데이트할 장소의 DB ID (PK)", required = true, example = "1")
            @PathVariable Integer placeId) {
        PlaceResponseDto updatedPlace = placeService.updatePlace(placeId);
        return ResponseEntity.ok(updatedPlace);
    }
}

