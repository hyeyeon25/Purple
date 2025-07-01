package Purple.Purple.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Itinerary, which functions as a "folder" for a travel plan.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Itinerary")
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itinerary_id")
    private Integer itineraryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "itinerary_title", nullable = false, length = 100)
    private String itineraryTitle;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "itinerary_generated_by_ai", nullable = false)
    private boolean itineraryGeneratedByAi;

    @CreationTimestamp
    @Column(name = "itinerary_created_at", nullable = false, updatable = false)
    private LocalDateTime itineraryCreatedAt;

    // The list of places in this itinerary, ordered by their visit sequence.
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("visitOrder ASC")
    private List<ItineraryPlace> itineraryPlaces = new ArrayList<>();
}