package Purple.Purple.folder.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class FolderCreateRequestDto {
	private LocalDate date;
	private Integer neighborhoodId; // 동네 ID (폴더명 자동 생성에 사용)
}


