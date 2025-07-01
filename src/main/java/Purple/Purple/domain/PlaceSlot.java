package Purple.Purple.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * A composite key class for the PlaceSlot entity.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
class PlaceSlotId implements Serializable {

    @Column(name = "place_id")
    private Integer placeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", length = 50, nullable = false)
    private SlotType slotType;
}

/**
 * Maps a Place to a suitable time slot (e.g., "Good for Lunch").
 * This entity uses a composite key.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Place_Slot")
public class PlaceSlot {

    @EmbeddedId
    private PlaceSlotId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("placeId") // Maps the placeId part of the composite key.
    @JoinColumn(name = "place_id")
    private Place place;
}