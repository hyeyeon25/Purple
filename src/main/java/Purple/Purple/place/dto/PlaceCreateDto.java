package Purple.Purple.place.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlaceCreateDto {
    @NotBlank(message = "kakaoPlaceId는 필수입니다.")
    private String kakaoPlaceId;

    @NotBlank(message = "placeName은 필수입니다.")
    private String placeName;

    @NotBlank(message = "placeCategory는 필수입니다.")
    private String placeCategory;

    @NotBlank(message = "address는 필수입니다.")
    private String address;

    @NotNull(message = "latitude는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "유효하지 않은 위도입니다.")
    @DecimalMax(value = "90.0", message = "유효하지 않은 위도입니다.")
    private Double latitude;

    @NotNull(message = "longitude는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "유효하지 않은 경도입니다.")
    @DecimalMax(value = "180.0", message = "유효하지 않은 경도입니다.")
    private Double longitude;

    private String summary;

    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    @Builder.Default
    private Boolean isIndoor = true;

    @Builder.Default
    @Positive(message = "체류 시간은 1분 이상이어야 합니다.")
    private Integer stayDurationMinutes = 60;

    private RecommendedSlot recommendedSlot;

    @NotNull(message = "neighborhoodId는 필수입니다.")
    private Integer neighborhoodId;

    private String googlePlaceId;
}
