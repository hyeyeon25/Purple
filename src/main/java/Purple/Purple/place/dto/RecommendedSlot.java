package Purple.Purple.place.dto;

/**
 * 추천 시간대(대표 슬롯)
 * - 비즈니스 정책에 따라 카테고리/패턴으로 추론하거나
 *   운영툴/관리자 입력으로 설정할 수 있습니다.
 * - 스키마가 단일 recommended_slot 문자열일 경우, 이 enum을 String으로 교체해도 됩니다.
 */
public enum RecommendedSlot {
    BREAKFAST,
    LUNCH,
    AFTERNOON,
    DINNER,
    NIGHT
}
