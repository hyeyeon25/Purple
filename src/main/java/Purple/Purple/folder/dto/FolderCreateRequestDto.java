package Purple.Purple.folder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "폴더 생성 요청 DTO")
public class FolderCreateRequestDto {
	@Schema(description = "폴더 이름 (무시됨, 항상 자동 생성: '{날짜} {동네명}' 형식)", example = "2025-01-15 불당동", deprecated = true)
	private String folderTitle;
	
	@Schema(description = "여행 날짜 (폴더명 자동 생성에 사용)", example = "2025-01-15", required = true)
	private LocalDate date;
	
	@Schema(description = "동네 ID (폴더명 자동 생성에 사용)", example = "1", required = true)
	private Integer neighborhoodId;
	
	@Schema(description = "선택한 장소 ID 리스트 (placeId만 전송, DB에서 장소 정보 조회)", example = "[685, 686, 687]")
	private List<Integer> placeIds;
}


