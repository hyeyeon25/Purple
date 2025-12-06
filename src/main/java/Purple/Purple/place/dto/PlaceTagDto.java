package Purple.Purple.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "장소 태그 정보 DTO")
public class PlaceTagDto {

    @Schema(description = "장소 ID", example = "1")
    private Integer placeId;

    @Schema(description = "장소명", example = "스타벅스 천안점")
    private String placeName;

    @Schema(description = "태그 리스트", example = "[\"카페\", \"실내\", \"오후추천\"]")
    private List<String> tags;

    @Schema(description = "태그 원본 문자열 (쉼표 구분)", example = "카페,실내,오후추천")
    private String tagsRaw;
}