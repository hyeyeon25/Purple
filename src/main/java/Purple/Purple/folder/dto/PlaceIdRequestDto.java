package Purple.Purple.folder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "장소 ID 요청 DTO (placeId만 받아서 DB에서 장소 정보를 조회합니다)")
public class PlaceIdRequestDto {
	@Schema(description = "장소 ID (DB에 이미 존재하는 장소만 추가 가능)", example = "685", required = true)
	private Integer placeId;
}

