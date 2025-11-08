package Purple.Purple.preferences.service;

import Purple.Purple.preferences.dto.UserPreferenceRequest;
import Purple.Purple.preferences.dto.UserPreferenceResponse;
import Purple.Purple.preferences.entity.PreferencesEntity;
import Purple.Purple.preferences.repository.PreferencesRepository;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferencesService {

    private final PreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // 고정된 태그 사전 (NeighborhoodRecommendationServiceImpl과 동일)
    private static final List<String> TAG_DICTIONARY = Arrays.asList(
            "음식점", "카페", "액티비티", "문화",
            "실내", "실외",
            "아침추천", "점심추천", "오후추천", "저녁추천", "밤추천",
            "데이트", "가족", "친구", "혼자"
    );
    /**
     * 등록/수정 겸용 (UPSERT)
     * - 최초 호출: userId로 엔티티가 없으면 새로 생성
     * - 이후 호출: 기존 엔티티를 업데이트
     */
    @Transactional
    public UserPreferenceResponse savePreferences(Long userId, UserPreferenceRequest req) {

        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        PreferencesEntity entity = preferencesRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    PreferencesEntity e = new PreferencesEntity();
                    e.setUser(user);
                    return e;
                });

        entity.setFoodPreference(req.getFoodPreference());
        entity.setDesertPreference(req.getDesertPreference());
        entity.setCulturePreference(req.getCulturePreference());
        entity.setTimePreference(req.getTimePreference());
        entity.setIndoorPreference(req.isIndoorPreference());
        entity.setExtrovertPreference(req.isExtrovertPreference());

        // 선호도를 태그로 변환 후 벡터 생성 및 저장
        try {
            List<String> userTags = convertPreferencesToTags(entity);
            List<Double> userVector = generateVector(userTags);
            List<Double> normalizedVector = normalizeVector(userVector);
            String vectorJson = objectMapper.writeValueAsString(normalizedVector);
            entity.setTagVector(vectorJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("태그 벡터 생성 중 오류가 발생했습니다.", e);
        }

        PreferencesEntity saved = preferencesRepository.save(entity);
        return toResponse(saved);
    }

    /**
     * 조회
     */
    @Transactional(readOnly = true)
    public UserPreferenceResponse getPreferences(Long userId) {
        PreferencesEntity entity = preferencesRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 성향 정보가 없습니다."));
        return toResponse(entity);
    }

    private UserPreferenceResponse toResponse(PreferencesEntity e) {
        return UserPreferenceResponse.builder()
                .userid(e.getUser().getUserId())
                .foodPreference(e.getFoodPreference())
                .desertPreference(e.getDesertPreference())
                .culturePreference(e.getCulturePreference())
                .timePreference(e.getTimePreference())
                .indoorPreference(e.isIndoorPreference())
                .extrovertPreference(e.isExtrovertPreference())
                .build();
    }

    /**
     * 숫자형 선호도를 태그 리스트로 변환
     */
    private List<String> convertPreferencesToTags(PreferencesEntity entity) {
        List<String> tags = new ArrayList<>();

        // 1. 장소 타입 선호도
        // foodPreference: 0-6 중 하나라도 있으면 음식점 태그 추가
        if (entity.getFoodPreference() >= 0) {
            tags.add("음식점");
        }

        // desertPreference: 50 이상이면 카페 태그 추가
        if (entity.getDesertPreference() >= 50) {
            tags.add("카페");
        }

        // culturePreference: 50 이상이면 문화 태그 추가
        if (entity.getCulturePreference() >= 50) {
            tags.add("문화");
        }

        // 2. 실내/실외 선호도
        if (entity.isIndoorPreference()) {
            tags.add("실내");
        } else {
            tags.add("실외");
        }

        // 3. 시간대 선호도
        // timePreference: 0=아침, 1=점심, 2=저녁, 3=밤
        switch (entity.getTimePreference()) {
            case 0:
                tags.add("아침추천");
                break;
            case 1:
                tags.add("점심추천");
                tags.add("오후추천");
                break;
            case 2:
                tags.add("저녁추천");
                break;
            case 3:
                tags.add("밤추천");
                break;
        }

        // 4. 동행 선호도 (외향성 기반)
        if (entity.isExtrovertPreference()) {
            // 외향적: 데이트, 가족, 친구
            tags.add("데이트");
            tags.add("가족");
            tags.add("친구");
        } else {
            // 내향적: 혼자
            tags.add("혼자");
        }

        return tags;
    }

    /**
     * 원-핫 인코딩으로 벡터 생성
     * NeighborhoodRecommendationServiceImpl의 generateVector와 동일한 로직
     */
    private List<Double> generateVector(List<String> tags) {
        List<Double> vector = new ArrayList<>();

        for (String dictTag : TAG_DICTIONARY) {
            if (tags.contains(dictTag)) {
                vector.add(1.0);
            } else {
                vector.add(0.0);
            }
        }

        return vector;
    }

    /**
     * L2 정규화
     * NeighborhoodRecommendationServiceImpl의 normalizeVector와 동일한 로직
     */
    private List<Double> normalizeVector(List<Double> vector) {
        // L2 norm 계산
        double l2Norm = Math.sqrt(vector.stream()
                .mapToDouble(v -> v * v)
                .sum());

        // 0으로 나누는 것을 방지
        if (l2Norm == 0) {
            return vector;
        }

        // 정규화
        return vector.stream()
                .map(v -> v / l2Norm)
                .collect(Collectors.toList());
    }
}
