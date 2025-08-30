package Purple.Purple.places.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "동네 내 추천 장소 리스트 응답 DTO")
public class PlacesResponseDto {
    @Schema(description = "지역명", example = "천안시")
    private String regionName;

    @Schema(description = "동네 ID", example = "12")
    private Integer neighborhoodId;

    @Schema(description = "동네명", example = "신부동")
    private String neighborhoodName;

    @Schema(description = "페이지 번호", example = "1")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "전체 결과 수", example = "42")
    private long total;

    @Schema(description = "장소 목록")
    private List<PlaceSummaryDto> places;
}