package Purple.Purple.place.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalTime;

/**
 * Normalized DTO: 우리 서비스의 "장소 저장 표준" 데이터 그릇
 *
 * - 외부 API(Kakao/Google)에서 온 원본 응답(Inbound DTO)을
 *   서비스/매퍼 레이어에서 가공하여 이 형태로 정리합니다.
 * - 이 DTO는 DB 엔티티(PlaceEntity)로 변환하기 좋게 설계되었습니다.
 * - 기본값/도메인 정책(예: isIndoor 기본 true, 체류시간 60분, 추천슬롯 추론 등)을
 *   이 단계에서 채워 넣습니다.
 *
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlaceCreateDto {

    /** 외부 식별자(카카오 고유 장소 ID) — 멱등성/중복 체크에 사용 */
    @NotBlank(message = "kakaoPlaceId는 필수입니다.")
    private String kakaoPlaceId;

    /** 기본 정보 */
    @NotBlank(message = "placeName은 필수입니다.")
    private String placeName;

    /** 예: "음식점 > 카페 > ..." 같은 원문 카테고리 전체 문자열 저장 권장 */
    @NotBlank(message = "placeCategory는 필수입니다.")
    private String placeCategory;

    /** 도로명 주소 우선, 없으면 지번 주소 — 매퍼에서 선택 후 세팅 */
    @NotBlank(message = "address는 필수입니다.")
    private String address;

    /** 위도(y), 경도(x) — 문자열 파싱 성공 여부 매퍼에서 검증 */
    @NotNull(message = "latitude는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "유효하지 않은 위도입니다.")
    @DecimalMax(value = "90.0", message = "유효하지 않은 위도입니다.")
    private Double latitude;

    @NotNull(message = "longitude는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "유효하지 않은 경도입니다.")
    @DecimalMax(value = "180.0", message = "유효하지 않은 경도입니다.")
    private Double longitude;

    /** 요약/소개 — 없으면 null/빈문자 허용 */
    private String summary;

    /** 영업시간(대표값) — 구글 응답을 정책에 맞게 보강하여 세팅 */
    private LocalTime openTime;
    private LocalTime closeTime;

    /** 브레이크 타임(선택) — 없으면 null */
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    /** 실내/야외 — 외부에서 정보 없으면 기본 true로 시작 후 수정 가능 */
    @Builder.Default
    private Boolean isIndoor = true;

    /** 예상 체류 시간(분) — 기본 60 */
    @Builder.Default
    @Positive(message = "체류 시간은 1분 이상이어야 합니다.")
    private Integer stayDurationMinutes = 60;

    /** 추천 시간대 — enum 권장(LUNCH/DINNER/AFTERNOON 등) */
    private RecommendedSlot recommendedSlot;

    /** 소속 동네(FK) — 저장 시 필요(좌표로 역지오코딩 또는 호출 파라미터로 주입) */
    @NotNull(message = "neighborhoodId는 필수입니다.")
    private Integer neighborhoodId;

    /** (선택) 구글 고유 장소 ID — 영업시간 재동기화 등에 유용 */
    private String googlePlaceId;
}
