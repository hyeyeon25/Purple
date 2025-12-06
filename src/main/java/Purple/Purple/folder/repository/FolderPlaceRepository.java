package Purple.Purple.folder.repository;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.folder.domain.FolderPlace;
import Purple.Purple.place.entity.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderPlaceRepository extends JpaRepository<FolderPlace, Integer> {
	boolean existsByFolderAndPlace(Folder folder, PlaceEntity place);
	Optional<FolderPlace> findByFolderAndPlace(Folder folder, PlaceEntity place);
	List<FolderPlace> findAllByFolder(Folder folder);
}


