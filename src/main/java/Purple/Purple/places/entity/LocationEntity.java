package Purple.Purple.places.entity;

import Purple.Purple.neighborhood.entity.NeighborhoodEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Integer id;

    @Column(name = "location_name", nullable = false)
    private String name;

    @Column(name = "location_category", nullable = false)
    private String category;

    @Column(name = "location_address", nullable = false)
    private String address;

    @Column(name = "location_latitude", nullable = false)
    private Double latitude;

    @Column(name = "location_longitude", nullable = false)
    private Double longitude;

    @Column(name = "location_summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "location_open_time")
    private LocalTime openTime;

    @Column(name = "location_close_time")
    private LocalTime closeTime;

    @Column(name = "is_indoor")
    private Boolean indoor;

    @Column(name = "location_place_type")
    private String placeType;

    // --- 관계 매핑 ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id")
    private NeighborhoodEntity neighborhood;

//    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ReviewEntity> reviews = new ArrayList<>(); // ReviewEntity가 있다고 가정
//
//    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<LocationLikeEntity> likes = new ArrayList<>(); // LocationLikeEntity가 있다고 가정
//
//    @ManyToMany
//    @JoinTable(
//            name = "location_tag",
//            joinColumns = @JoinColumn(name = "location_id"),
//            inverseJoinColumns = @JoinColumn(name = "tag_id")
//    )
//    private Set<TagEntity> tags = new HashSet<>(); // TagEntity가 있다고 가정
}