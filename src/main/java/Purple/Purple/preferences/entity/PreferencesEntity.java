package Purple.Purple.preferences.entity;


import Purple.Purple.user.entity.UserPersonalInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "preferences")
public class PreferencesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                   // 새 PK

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private UserPersonalInfo user;            // 사용자 식별자 (유니크)

    @Column(nullable = false)
    private int extrovertPreference; // 외향 성향 코드 (0~100, 높을수록 외향적)

    @Column(nullable = false)
    private int indoorPreference;  // 실내 선호도 코드 (0~100, 높을수록 실내 선호)

    @ElementCollection
    @CollectionTable(
            name = "preference_time",
            joinColumns = @JoinColumn(name = "preference_id")
    )
    @Column(name = "time_code")
    private List<Integer> timePreferences = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "preference_food",
            joinColumns = @JoinColumn(name = "preference_id")
    )
    @Column(name = "food_code")
    private List<Integer> foodPreferences = new ArrayList<>();//음식 선호도

    @Column(nullable = false)
    private int desertPreference;//디저트 선호도

    @Column(nullable = false)
    private int culturePreference;//문화 선호도

    @Column(nullable = false)
    private int activityPreference; // 활동 선호도 코드 (예 0~100)

    @Column(columnDefinition = "TEXT")
    private String tagVector; // 사용자 선호도 태그 벡터 (JSON 배열 형식)

}


