package Purple.Purple.folder.dto;

import Purple.Purple.folder.domain.Folder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class FolderSummaryResponseDto {
	private Integer folderId;
	private String folderTitle;
	private LocalDate date;
	private int placeCount;

	public FolderSummaryResponseDto(Folder folder) {
		this.folderId = folder.getFolderId();
		this.folderTitle = folder.getFolderTitle();
		this.date = folder.getDate();
		this.placeCount = folder.getFolderPlaces() == null ? 0 : folder.getFolderPlaces().size();
	}
}


