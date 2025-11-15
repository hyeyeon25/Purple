package Purple.Purple.itinerery.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ItineraryUpdateRequestDto {
	/**
	 * 방문할 장소 ID 리스트 (순서대로)
	 * 사용자가 드래그로 변경한 순서대로 장소 ID를 나열합니다.
	 * 폴더의 모든 장소를 포함해야 합니다.
	 */
	private List<Integer> placeIds;
}

