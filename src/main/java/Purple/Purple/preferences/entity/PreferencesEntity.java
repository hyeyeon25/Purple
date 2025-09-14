package Purple.Purple.preferences.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(
        name = "preferences",
        uniqueConstraints = @UniqueConstraint(name = "uq_preferences_user", columnNames = "user_id")
)
public class PreferencesEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                   // 새 PK

    @Column(name = "user_id", nullable = false)
    private Integer userid;            // 사용자 식별자 (유니크)

    @Column(nullable = false, length = 6)
    private int foodPreference;
    @Column(nullable = false, length = 100)
    private int desertPreference;
    @Column(nullable = false, length = 100)
    private int culturePreference;

    @Column(nullable = false, length = 3)
    private int timePreference;

    @Column(nullable = false)
    private boolean indoorPreference;  // true=실내, false=실외

    @Column(nullable = false)
    private boolean extrovertPreference; // true=외향, false=내향
}

 /*@ElementCollection
    @CollectionTable(
            name = "preference_time", // 중복 시간대 저장 테이블
            joinColumns = @JoinColumn(name = "userid")
    )
    @Column(name = "time_code", nullable = false)
    private List<Integer> timePreference;*/

