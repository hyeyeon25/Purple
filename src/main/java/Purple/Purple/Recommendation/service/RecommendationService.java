package Purple.Purple.Recommendation.service;

import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.preferences.entity.PreferencesEntity;

public class RecommendationService {
    private static final int NOT_APPLICABLE = -1;

    // 사용자 선호도와 장소 특성을 기반으로 최종 매칭 점수를 계산
     private double calculateUserPlaceScore(PreferencesEntity prefs, PlaceEntity place) {
        double totalScore = 0.0;

        // 1. 음식 점수 계산
        int placeFoodCode = place.getScoreFood();
        if (placeFoodCode != NOT_APPLICABLE) { // 이 장소가 음식 관련 장소일 경우
            if (prefs.getFoodPreference() == placeFoodCode) {
                totalScore += 100.0; // 높은 점수(100점) 부여
            } else {
                // 음식 코드는 다르지만, 어쨌든 음식점이므로 기본 점수 부여
                totalScore += 20.0; // 기본 점수(20점) 부여
            }
        }

        // 2. 카페(디저트) 점수 계산
        if (place.getScoreCafe() != NOT_APPLICABLE) { // 이 장소가 카페 관련 장소일 경우
            // 사용자의 디저트 선호도(0~100)를 점수에 직접 반영
            totalScore += prefs.getDesertPreference();
        }

        // 3. 문화 점수 계산
        if (place.getScoreCulture() != NOT_APPLICABLE) { // 이 장소가 문화 관련 장소일 경우
            // 사용자의 문화 선호도(0~100)를 점수에 직접 반영
            totalScore += prefs.getCulturePreference();
        }

        // 4. 활동 점수 계산
        if (place.getScoreActivity() != NOT_APPLICABLE) { // 이 장소가 활동 관련 장소일 경우
            // 외향적인(extrovert) 성향의 사용자에게 활동 장소 점수 추가
            if (prefs.isExtrovertPreference()) {
                totalScore += 50.0; // 보너스 점수(50점) 부여
            } else {
                // 내향적인 사용자라도 활동을 할 수 있으므로 기본 점수 부여
                totalScore += 10.0;
            }
        }

        // 5. 추가 가중치 적용 (실내/실외 선호도)
        // 장소의 실내/외 정보(isIndoor)가 있고, 사용자의 선호도와 다를 경우 점수 조정
        if (place.getIsIndoor() != null && prefs.isIndoorPreference() != place.getIsIndoor()) {
            totalScore *= 0.7; // 선호도와 다르면 전체 점수의 30%를 감소시키는 페널티 적용
        }

        return totalScore;
    }

}
