package Purple.Purple.Neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 장소 추천 응답 DTO
 *
 * 특정 동네 내에서 사용자 선호도와 일치하는 장소를
 * 유사도 점수와 함께 반환합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "특정 동네 내 장소 추천 응답 DTO")
public class PlaceRecommendationResponseDto {

    @Schema(description = "장소 ID (DB PK)", example = "101")
    private Integer placeId;

    @Schema(description = "카카오 장소 ID", example = "18231234")
    private String kakaoPlaceId;

    @Schema(description = "장소 이름", example = "마초쉐프 천안점")
    private String placeName;

    @Schema(description = "장소 카테고리", example = "음식점 > 양식 > 이탈리안")
    private String placeCategory;

    @Schema(description = "주소", example = "충남 천안시 동남구 신부동 123-4")
    private String address;

    @Schema(description = "위도", example = "36.8184")
    private Double latitude;

    @Schema(description = "경도", example = "127.1528")
    private Double longitude;

    @Schema(description = "장소의 특징 태그 (CSV)", example = "음식점,실내,저녁추천,데이트")
    private String tags;

    @Schema(description = "사용자 선호도와의 유사도 점수 (0.0 ~ 1.0)", example = "0.912")
    private Double similarityScore;

    @Schema(description = "실내/실외 여부", example = "true")
    private Boolean isIndoor;

    @Schema(description = "추천 시간대 (카테고리 기반 추론)", example = "DINNER")
    private String recommendedSlot;

    @Schema(description = "평균 체류 시간 (분)", example = "90")
    private Integer stayDurationMinutes;
}