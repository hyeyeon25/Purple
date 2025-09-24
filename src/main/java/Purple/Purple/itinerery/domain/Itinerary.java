package Purple.Purple.itinerery.domain;

import Purple.Purple.user.entity.UserPersonalInfo; // import 변경
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private UserPersonalInfo user; // ★★★ User -> UserPersonalInfo 로 타입 변경 ★★★

    @Column(name = "itinerary_title", nullable = false, length = 100)
    private String itineraryTitle;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "itinerary_generated_by_ai", nullable = false)
    private boolean itineraryGeneratedByAi;

    @CreationTimestamp
    @Column(name = "itinerary_created_at", nullable = false, updatable = false)
    private LocalDateTime itineraryCreatedAt;

    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("visitOrder ASC")
    private List<ItineraryPlace> itineraryPlaces = new ArrayList<>();
}