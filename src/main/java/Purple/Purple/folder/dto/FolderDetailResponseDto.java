package Purple.Purple.folder.dto;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.itinerery.domain.ItineraryPlace;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class FolderDetailResponseDto {
	private Integer folderId;
	private String folderTitle;
	private LocalDate date;
	private Integer neighborhoodId;
	private List<FolderPlaceResponseDto> places;
	private List<Integer> routePlaceIdsInOrder;

	public FolderDetailResponseDto(Folder folder) {
		this.folderId = folder.getFolderId();
		this.folderTitle = folder.getFolderTitle();
		this.date = folder.getDate();
		this.neighborhoodId = folder.getNeighborhoodId();
		this.places = folder.getFolderPlaces().stream()
				.map(FolderPlaceResponseDto::new)
				.collect(Collectors.toList());
		if (folder.getItinerary() != null) {
			this.routePlaceIdsInOrder = folder.getItinerary().getItineraryPlaces().stream()
					.sorted((a, b) -> Integer.compare(
							a.getVisitOrder() == null ? 0 : a.getVisitOrder(),
							b.getVisitOrder() == null ? 0 : b.getVisitOrder()))
					.map(ip -> ip.getPlace().getPlaceId())
					.collect(Collectors.toList());
		}
	}
}


