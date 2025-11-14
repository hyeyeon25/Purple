package Purple.Purple.preferences.entity;


import Purple.Purple.user.entity.UserPersonalInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private int foodPreference;

    @Column(nullable = false)
    private int desertPreference;

    @Column(nullable = false)
    private int culturePreference;

    @Column(nullable = false)
    private int timePreference;

    @Column(nullable = false)
    private boolean indoorPreference;  // true=실내, false=실외

    @Column(nullable = false)
    private boolean extrovertPreference; // true=외향, false=내향

    @Column(nullable = false)
    private int activityPreference; // 활동 선호도 코드 (예 0~100)

    @Column(columnDefinition = "TEXT")
    private String tagVector; // 사용자 선호도 태그 벡터 (JSON 배열 형식)

}

 /*@ElementCollection
    @CollectionTable(
            name = "preference_time", // 중복 시간대 저장 테이블
            joinColumns = @JoinColumn(name = "userid")
    )
    @Column(name = "time_code", nullable = false)
    private List<Integer> timePreference;*/

