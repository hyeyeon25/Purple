package Purple.Purple.preferences.service;

import Purple.Purple.preferences.dto.UserPreferenceRequest;
import Purple.Purple.preferences.dto.UserPreferenceResponse;
import Purple.Purple.preferences.entity.PreferencesEntity;
import Purple.Purple.preferences.repository.PreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferencesService {

    private final PreferencesRepository preferencesRepository;

    /**
     * 등록/수정 겸용 (UPSERT)
     * - 최초 호출: userId로 엔티티가 없으면 새로 생성
     * - 이후 호출: 기존 엔티티를 업데이트
     */
    @Transactional
    public UserPreferenceResponse savePreferences(int userId, UserPreferenceRequest req) {

        PreferencesEntity entity = preferencesRepository.findByUserid(userId)
                .orElseGet(() -> {
                    PreferencesEntity e = new PreferencesEntity();
                    e.setUserid(userId);  // ← 이제는 일반 컬럼이라 직접 세팅 OK
                    return e;
                });

        entity.setFoodPreference(req.getFoodPreference());
        entity.setDesertPreference(req.getDesertPreference());
        entity.setCulturePreference(req.getCulturePreference());
        entity.setTimePreference(req.getTimePreference());
        entity.setIndoorPreference(req.isIndoorPreference());
        entity.setExtrovertPreference(req.isExtrovertPreference());

        PreferencesEntity saved = preferencesRepository.save(entity);
        return toResponse(saved);
    }

    /**
     * 조회
     */
    @Transactional(readOnly = true)
    public UserPreferenceResponse getPreferences(int userId) {
        PreferencesEntity entity = preferencesRepository.findByUserid(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 성향 정보가 없습니다."));
        return toResponse(entity);
    }

    private UserPreferenceResponse toResponse(PreferencesEntity e) {
        return UserPreferenceResponse.builder()
                .userid(e.getUserid())
                .foodPreference(e.getFoodPreference())
                .desertPreference(e.getDesertPreference())
                .culturePreference(e.getCulturePreference())
                .timePreference(e.getTimePreference())
                .indoorPreference(e.isIndoorPreference())
                .extrovertPreference(e.isExtrovertPreference())
                .build();
    }
}
