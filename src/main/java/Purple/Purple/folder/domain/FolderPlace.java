package Purple.Purple.folder.domain;

import Purple.Purple.place.entity.PlaceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "folder_place", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"folder_id", "place_id"})
})
public class FolderPlace {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "folder_place_id")
	private Integer folderPlaceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "folder_id", nullable = false)
	private Folder folder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "place_id", nullable = false)
	private PlaceEntity place;
}


