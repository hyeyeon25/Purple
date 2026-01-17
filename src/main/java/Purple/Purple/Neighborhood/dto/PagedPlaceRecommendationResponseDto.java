package Purple.Purple.Neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 장소 추천 페이지네이션 응답 DTO
 *
 * 특정 동네 내에서 사용자 선호도와 일치하는 장소를
 * 페이지 단위로 반환
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "장소 추천 페이지네이션 응답 DTO")
public class PagedPlaceRecommendationResponseDto {

    @Schema(description = "장소 추천 목록")
    private List<PlaceRecommendationResponseDto> content;

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int currentPage;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "전체 요소 수", example = "45")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "이전 페이지 존재 여부", example = "false")
    private boolean hasPrevious;
}