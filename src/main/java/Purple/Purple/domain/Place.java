package Purple.Purple.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Place, the fundamental building block of an itinerary.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Place")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Integer placeId;

    @Column(name = "kakao_place_id", unique = true, length = 50)
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

    @Column(name = "score_food", nullable = false)
    private short scoreFood;

    @Column(name = "score_cafe", nullable = false)
    private short scoreCafe;

    @Column(name = "score_activity", nullable = false)
    private short scoreActivity;

    @Column(name = "score_culture", nullable = false)
    private short scoreCulture;

    @Lob
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
    private Integer stayDurationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id")
    private Neighborhood neighborhood;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaceSlot> availableSlots = new HashSet<>();
}