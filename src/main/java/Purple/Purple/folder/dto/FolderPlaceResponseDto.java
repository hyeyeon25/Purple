package Purple.Purple.folder.dto;

import Purple.Purple.folder.domain.FolderPlace;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FolderPlaceResponseDto {
	private Integer placeId;
	private String placeName;
	private String address;

	public FolderPlaceResponseDto(FolderPlace fp) {
		this.placeId = fp.getPlace().getPlaceId();
		this.placeName = fp.getPlace().getPlaceName();
		this.address = fp.getPlace().getAddress();
	}
}


