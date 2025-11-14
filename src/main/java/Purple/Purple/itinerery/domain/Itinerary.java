package Purple.Purple.itinerery.domain;

import Purple.Purple.folder.domain.Folder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

	@PrePersist
	protected void onCreate() {
		if (itineraryGeneratedByAi == null) {
			itineraryGeneratedByAi = false;
		}
	}

	@Column(name = "itinerary_generated_by_ai", nullable = false)
	private Boolean itineraryGeneratedByAi = false;

	@OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("visitOrder ASC")
	private List<ItineraryPlace> itineraryPlaces = new ArrayList<>();
}