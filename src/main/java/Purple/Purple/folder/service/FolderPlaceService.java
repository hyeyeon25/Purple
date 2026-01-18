package Purple.Purple.folder.service;

import Purple.Purple.folder.domain.Folder;
import Purple.Purple.folder.domain.FolderPlace;
import Purple.Purple.folder.exception.InvalidFolderRequestException;
import Purple.Purple.folder.repository.FolderPlaceRepository;
import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.itinerery.domain.ItineraryPlace;
import Purple.Purple.itinerery.repository.ItineraryPlaceRepository;
import Purple.Purple.itinerery.repository.ItineraryRepository;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//  폴더 내 장소 관리 담당 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class FolderPlaceService {

    private final FolderPlaceRepository folderPlaceRepository;
    private final PlaceRepository placeRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryPlaceRepository itineraryPlaceRepository;

// 폴더에 장소 추가
    @Transactional
    public void addPlaceToFolder(Folder folder, Integer placeId) {
        PlaceEntity place = validatePlace(placeId);

        // 중복 체크
        if (folderPlaceRepository.existsByFolderAndPlace(folder, place)) {
            log.info("장소가 이미 폴더에 존재합니다. folderId={}, placeId={}", folder.getFolderId(), placeId);
            return;
        }

        // 폴더에 장소 추가
        FolderPlace folderPlace = createFolderPlace(folder, place);
        folderPlaceRepository.save(folderPlace);

        // Itinerary에 장소 추가
        addPlaceToItinerary(folder, place);
    }

    // 폴더에 여러 장소 추가
    @Transactional
    public void addPlacesToFolder(Folder folder, List<Integer> placeIds) {
        if (placeIds == null || placeIds.isEmpty()) {
            return;
        }

        for (Integer placeId : placeIds) {
            if (placeId == null || placeId == 0) {
                continue;
            }
            addPlaceToFolder(folder, placeId);
        }
    }

    // 폴더에서 장소 삭제
    @Transactional
    public void removePlaceFromFolder(Folder folder, Integer placeId) {
        PlaceEntity place = validatePlace(placeId);

        FolderPlace folderPlace = folderPlaceRepository.findByFolderAndPlace(folder, place)
                .orElseThrow(() -> new InvalidFolderRequestException("폴더에 없는 장소입니다. placeId=" + placeId));

        // 폴더에서 장소 삭제
        folderPlaceRepository.delete(folderPlace);

        // Itinerary에서도 삭제
        removePlaceFromItinerary(folder, placeId);
    }

     // 폴더의 모든 장소 조회
    @Transactional(readOnly = true)
    public List<FolderPlace> getFolderPlacesWithPlaces(Folder folder) {
        List<FolderPlace> folderPlaces = folderPlaceRepository.findAllByFolder(folder);

        // LAZY 로딩 초기화
        for (FolderPlace fp : folderPlaces) {
            fp.getPlace().getPlaceId();
        }

        return folderPlaces;
    }

    // 장소 유효성 검증
    private PlaceEntity validatePlace(Integer placeId) {
        if (placeId == null || placeId == 0) {
            throw new InvalidFolderRequestException("유효한 장소 ID가 필요합니다. placeId=" + placeId);
        }

        return placeRepository.findById(placeId)
                .orElseThrow(() -> new InvalidFolderRequestException("해당 장소를 찾을 수 없습니다. id=" + placeId));
    }

    // FolderPlace 엔티티 생성
    private FolderPlace createFolderPlace(Folder folder, PlaceEntity place) {
        FolderPlace folderPlace = new FolderPlace();
        folderPlace.setFolder(folder);
        folderPlace.setPlace(place);
        return folderPlace;
    }

    // Itinerary에 장소 추가
    private void addPlaceToItinerary(Folder folder, PlaceEntity place) {
        Itinerary itinerary = itineraryRepository.findByFolder(folder).orElse(null);
        if (itinerary == null) {
            return;
        }

        List<ItineraryPlace> existingPlaces = itineraryPlaceRepository.findByItinerary(itinerary);

        // LAZY 로딩 초기화
        for (ItineraryPlace ip : existingPlaces) {
            ip.getPlace().getPlaceId();
        }

        // 중복 체크
        boolean alreadyExists = existingPlaces.stream()
                .anyMatch(ip -> ip.getPlace().getPlaceId().equals(place.getPlaceId()));

        if (!alreadyExists) {
            int maxOrder = existingPlaces.stream()
                    .filter(ip -> ip.getVisitOrder() != null)
                    .mapToInt(ItineraryPlace::getVisitOrder)
                    .max()
                    .orElse(0);

            ItineraryPlace itineraryPlace = new ItineraryPlace();
            itineraryPlace.setItinerary(itinerary);
            itineraryPlace.setPlace(place);
            itineraryPlace.setVisitOrder(maxOrder + 1);
            itineraryPlaceRepository.save(itineraryPlace);
        }
    }

    //  Itinerary에서 장소 삭제
    private void removePlaceFromItinerary(Folder folder, Integer placeId) {
        Itinerary itinerary = itineraryRepository.findByFolder(folder).orElse(null);
        if (itinerary == null) {
            return;
        }

        List<ItineraryPlace> itineraryPlaces = itineraryPlaceRepository.findByItinerary(itinerary);

        // LAZY 로딩 초기화
        for (ItineraryPlace ip : itineraryPlaces) {
            ip.getPlace().getPlaceId();
        }

        itineraryPlaces.stream()
                .filter(ip -> ip.getPlace().getPlaceId().equals(placeId))
                .findFirst()
                .ifPresent(itineraryPlaceRepository::delete);
    }
}

