package Purple.Purple.itinerery.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RouteUpdateRequestDto {
	private List<Integer> placeIdsInOrder;
}


