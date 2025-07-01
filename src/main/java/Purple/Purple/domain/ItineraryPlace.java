package Purple.Purple.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * A join table entity that links a Place to an Itinerary,
 * defining the path's sequence and details.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Itinerary_Place", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"itinerary_id", "place_id"})
})
public class ItineraryPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itinerary_place_id")
    private Integer itineraryPlaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "visit_order")
    private Integer visitOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", length = 50)
    private SlotType slotType;

    @Lob
    private String memo;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;
}
