package Purple.Purple.places.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "장소 요약 정보 DTO")
public class PlaceSummaryDto {
    @Schema(description = "장소 ID", example = "101")
    private Integer locationId;

    @Schema(description = "장소명", example = "빈브라더스")
    private String locationName;

    @Schema(description = "카테고리", example = "카페")
    private String locationCategory;

    @Schema(description = "주소", example = "충남 천안시 동남구 유량동 258")
    private String address;

    @Schema(description = "위도", example = "36.8201")
    private Float latitude;

    @Schema(description = "경도", example = "127.1538")
    private Float longitude;

    @Schema(description = "평점", example = "4.6")
    private Float rating;

    @Schema(description = "리뷰 수", example = "523")
    private Integer reviewCount;

    @Schema(description = "사용자 성향 매칭 점수", example = "0.87")
    private Float matchScore;

    @Schema(description = "현재 영업 여부", example = "true")
    private boolean openNow;

    @Schema(description = "오늘 오픈 시간", example = "10:00")
    private String todayOpen;

    @Schema(description = "오늘 마감 시간", example = "20:00")
    private String todayClose;

    @Schema(description = "실내 여부", example = "true")
    private boolean isIndoor;

    @Schema(description = "장소 타입 (e.g., 조용한, 활기찬)", example = "조용한")
    private String placeType;

    @Schema(description = "태그 목록", example = "[\"성향:디저트\", \"분위기:데이트\"]")
    private List<String> tags;

    @Schema(description = "북마크 여부", example = "false")
    private boolean isBookmarked;

    @Schema(description = "현재 위치로부터의 거리(미터)", example = "180")
    private Integer distance;
}