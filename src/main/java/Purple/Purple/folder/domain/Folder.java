package Purple.Purple.folder.domain;

import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.user.entity.UserPersonalInfo;
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
@Table(name = "folder")
public class Folder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "folder_id")
	private Integer folderId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserPersonalInfo user;

	@Column(name = "folder_title", nullable = false, length = 100)
	private String folderTitle;

	@Column(name = "date")
	private LocalDate date;

	@Column(name = "neighborhood_id")
	private Integer neighborhoodId;

	@CreationTimestamp
	@Column(name = "folder_created_at", nullable = false, updatable = false)
	private LocalDateTime folderCreatedAt;

	@OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FolderPlace> folderPlaces = new ArrayList<>();

	@OneToOne(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Itinerary itinerary;
}


