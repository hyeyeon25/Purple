package Purple.Purple.itinerery.domain;

import Purple.Purple.folder.domain.Folder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "itinerary", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"folder_id"})
})
public class Itinerary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "itinerary_id")
	private Integer itineraryId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "folder_id", nullable = false)
	private Folder folder;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("visitOrder ASC")
	private List<ItineraryPlace> itineraryPlaces = new ArrayList<>();
}