package Purple.Purple.place.entity;

import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import Purple.Purple.folder.PlaceSlot;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "place")
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

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "tag_vector", columnDefinition = "TEXT")
    private String tagVector;

    // FK 매핑: Place → Neighborhood (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id", nullable = false)
    private NeighborhoodEntity neighborhood;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaceSlot> availableSlots = new HashSet<>();


    //1. mappedBy = "place": 실제 FK는 PlaceAnalysis 테이블에 있음 (현재 테이블 구조 영향 X)
    // 2. CascadeType.ALL: 이 장소(Place)가 삭제되면 분석 데이터도 같이 삭제됨 (데이터 관리 자동화)
    // 3. FetchType.LAZY: 상세 조회할 때만 분석 데이터를 가져옴 (평소 리스트 조회 시 성능 저하 방지)
    @OneToOne(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PlaceAnalysisEntity placeAnalysis;
}
