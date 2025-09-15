package Purple.Purple.preferences.service;

import Purple.Purple.preferences.dto.UserPreferenceRequest;
import Purple.Purple.preferences.dto.UserPreferenceResponse;
import Purple.Purple.preferences.entity.PreferencesEntity;
import Purple.Purple.preferences.repository.PreferencesRepository;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferencesService {

    private final PreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
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
}
