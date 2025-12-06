package Purple.Purple.folder.dto;

import Purple.Purple.folder.domain.FolderPlace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "폴더 내 장소 정보 DTO")
public class FolderPlaceResponseDto {
	@Schema(description = "장소 ID", example = "685")
	private Integer placeId;

	@Schema(description = "장소 이름", example = "스타벅스 천안점")
	private String placeName;

	@Schema(description = "주소", example = "충남 천안시 동남구...")
	private String address;

	@Schema(description = "위도 (지도 표시용)", example = "36.8184")
	private Double latitude;

	@Schema(description = "경도 (지도 표시용)", example = "127.1528")
	private Double longitude;

	@Schema(description = "다음 장소까지의 도보 거리 (미터)", example = "850")
	private Integer distanceToNext;

	@Schema(description = "다음 장소까지의 도보 소요 시간 (초)", example = "600")
	private Integer durationToNext;

	public FolderPlaceResponseDto(FolderPlace fp) {
		this.placeId = fp.getPlace().getPlaceId();
		this.placeName = fp.getPlace().getPlaceName();
		this.address = fp.getPlace().getAddress();
		this.latitude = fp.getPlace().getLatitude();
		this.longitude = fp.getPlace().getLongitude();
	}
}


