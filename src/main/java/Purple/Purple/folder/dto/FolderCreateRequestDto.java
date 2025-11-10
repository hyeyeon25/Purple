package Purple.Purple.folder.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class FolderCreateRequestDto {
	private String folderTitle;
	private LocalDate date;
}


