package Purple.Purple.folder.repository;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.user.entity.UserPersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Integer> {
	Optional<Folder> findByFolderIdAndUser(Integer folderId, UserPersonalInfo user);
	List<Folder> findAllByUserOrderByFolderIdDesc(UserPersonalInfo user);
}


