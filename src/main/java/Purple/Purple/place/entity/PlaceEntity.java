package Purple.Purple.place.entity;

import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import jakarta.persistence.*;
        import lombok.*;

        import java.time.LocalTime;

@Entity
@Table(name = "Place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Integer placeId;

    @Column(name = "kakao_place_id", length = 50, unique = true)
    private String kakaoPlaceId;

    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;

    @Column(name = "place_category", nullable = false, length = 50)
    private String placeCategory;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Builder.Default
    @Column(name = "score_food", nullable = false)
    private Integer scoreFood = 0;

    @Builder.Default
    @Column(name = "score_cafe", nullable = false)
    private Integer scoreCafe = 0;

    @Builder.Default
    @Column(name = "score_activity", nullable = false)
    private Integer scoreActivity = 0;

    @Builder.Default
    @Column(name = "score_culture", nullable = false)
    private Integer scoreCulture = 0;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    @Column(name = "is_indoor")
    private Boolean isIndoor;

    @Column(name = "stay_duration_minutes")
    private Integer stayDurationMinutes = 60;

    @Column(name = "recommended_slot", length = 50)
    private String recommendedSlot;

    // FK 매핑: Place → Neighborhood (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private NeighborhoodEntity neighborhood;
}
