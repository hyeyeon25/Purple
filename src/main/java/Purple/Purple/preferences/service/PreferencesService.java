package Purple.Purple.preferences.service;

import Purple.Purple.common.constants.TagDictionary;
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
        entity.setTimePreferences(req.getTimePreference());
        entity.setIndoorPreference(req.isIndoorPreference());
        entity.setExtrovertPreference(req.getExtrovertPreference());

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
                .timePreference(e.getTimePreferences())
                .indoorPreference(e.isIndoorPreference())
                .extrovertPreference(e.getExtrovertPreference())
                .build();
    }

    /**
     * 숫자형 선호도를 태그 리스트로 변환 (56개 세분화된 태그 사전 기반)
     */
    private List<String> convertPreferencesToTags(PreferencesEntity entity) {
        List<String> tags = new ArrayList<>();

        // 1. 음식 선호도 (세분화)
        // foodPreference: 0=한식, 1=양식, 2=일식, 3=중식, 4=아시안, 5=이색/퓨전, 6=분식
        switch (entity.getFoodPreference()) {
            case 0:
                tags.add("한식");
                break;
            case 1:
                tags.add("양식");
                break;
            case 2:
                tags.add("일식");
                break;
            case 3:
                tags.add("중식");
                break;
            case 4:
                tags.add("아시안");
                break;
            case 5:
                tags.add("이색/퓨전");
                break;
            case 6:
                tags.add("분식");
                break;
        }

        // 2. 디저트 선호도 (세분화)
        // desertPreference: 50 이상이면 카페/디저트 관련 태그 추가
        if (entity.getDesertPreference() >= 50) {
            tags.add("카페");
            tags.add("디저트");
            // 70 이상이면 베이커리도 추가 (높은 디저트 선호도)
            if (entity.getDesertPreference() >= 70) {
                tags.add("베이커리");
            }
        }

        // 3. 문화 선호도 (세분화)
        // culturePreference: 50 이상이면 문화생활 관련 태그 추가
        if (entity.getCulturePreference() >= 50) {
            tags.add("문화생활");
            tags.add("전시");
            // 70 이상이면 더 많은 문화 태그 추가
            if (entity.getCulturePreference() >= 70) {
                tags.add("공연");
                tags.add("미술관");
            }
        }

        // 4. 실내/실외 선호도
        if (entity.isIndoorPreference()) {
            tags.add("실내");
        } else {
            tags.add("실외");
            // 실외 선호 시 공원, 산책 태그 추가
            tags.add("공원");
            tags.add("산책");
        }

        // 5. 시간대 선호도 (세분화)
        // timePreference: 0=아침, 1=점심, 2=저녁, 3=밤
        for (int time : entity.getTimePreferences()) {
            switch (time) {
                case 0:
                    tags.add("아침");
                    break;
                case 1:
                    tags.add("점심");
                    tags.add("오후");
                    break;
                case 2:
                    tags.add("저녁");
                    break;
                case 3:
                    tags.add("밤");
                    break;
            }
        }

        // 6. 동행 선호도 (세분화)
        switch (entity.getExtrovertPreference()) {//추후 수정해야될 거 같긴해!!
            case 0: // 매우 외향적
                tags.add("친구");
                tags.add("단체");
                tags.add("활기찬");
                tags.add("사람많은");
                break;

            case 1: // 보통 외향적
                tags.add("친구");
                tags.add("단체");
                tags.add("활동적인");
                break;

            case 2: // 보통 내향적
                tags.add("혼자");
                tags.add("조용한");
                tags.add("차분한");
                break;

            case 3: // 매우 내향적
                tags.add("혼자");
                tags.add("고요한");
                tags.add("한적한");
                tags.add("잔잔한");
                break;

            default:
                tags.add("혼자");
                tags.add("조용한");
                break;
        }


        return tags;
    }

    /**
     * 원-핫 인코딩으로 벡터 생성
     * TagDictionary의 56개 태그 순서를 따름
     */
    private List<Double> generateVector(List<String> tags) {
        List<Double> vector = new ArrayList<>();

        for (String dictTag : TagDictionary.TAGS) {
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
