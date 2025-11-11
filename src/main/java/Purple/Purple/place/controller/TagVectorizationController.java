package Purple.Purple.place.controller;

import Purple.Purple.place.dto.BatchVectorizationResponseDto;
import Purple.Purple.place.dto.TagVectorizationRequestDto;
import Purple.Purple.place.dto.TagVectorizationResponseDto;
import Purple.Purple.place.service.TagVectorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tag Vectorization", description = "장소 태그 벡터화 API")
public class TagVectorizationController {

    private final TagVectorizationService tagVectorizationService;

    @Operation(
            summary = "단일 장소 태그 벡터화",
            description = "지정된 장소의 태그를 벡터화하여 DB에 저장합니다. 태그가 제공되지 않으면 장소 특성에서 자동으로 추출합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "벡터화 성공",
                    content = @Content(schema = @Schema(implementation = TagVectorizationResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장소를 찾을 수 없음"
            )
    })
    @PostMapping("/{placeId}/vectorize")
    public ResponseEntity<TagVectorizationResponseDto> vectorizeSinglePlace(
            @Parameter(description = "장소 ID", required = true, example = "1")
            @PathVariable Integer placeId,
            @Parameter(description = "벡터화 요청 DTO (선택적)")
            @RequestBody(required = false) TagVectorizationRequestDto requestDto
    ) {
        log.info("POST /api/v1/places/{}/vectorize - Request received", placeId);

        TagVectorizationResponseDto response = tagVectorizationService.vectorizeSinglePlace(placeId, requestDto);

        if (response.getSuccess()) {
            log.info("Vectorization successful for place ID: {}", placeId);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Vectorization failed for place ID: {}", placeId);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
            summary = "전체 장소 배치 벡터화",
            description = "모든 장소의 태그를 벡터화하여 DB에 저장합니다. 관리자용 API입니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "배치 벡터화 완료",
                    content = @Content(schema = @Schema(implementation = BatchVectorizationResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @PostMapping("/vectorize-all")
    public ResponseEntity<BatchVectorizationResponseDto> vectorizeAllPlaces() {
        log.info("POST /api/v1/places/vectorize-all - Request received");

        try {
            BatchVectorizationResponseDto response = tagVectorizationService.vectorizeAllPlaces();
            log.info("Batch vectorization completed. Success: {}, Failure: {}",
                    response.getSuccessCount(), response.getFailureCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Batch vectorization failed", e);
            BatchVectorizationResponseDto errorResponse = BatchVectorizationResponseDto.builder()
                    .totalPlaces(0)
                    .successCount(0)
                    .failureCount(0)
                    .processingTimeSeconds(0.0)
                    .message("배치 벡터화 실패: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
