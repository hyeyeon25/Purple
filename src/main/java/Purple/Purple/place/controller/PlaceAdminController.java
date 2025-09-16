package Purple.Purple.place.controller;

import Purple.Purple.place.dto.PlaceResponseDto;
import Purple.Purple.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/places")
@RequiredArgsConstructor
public class PlaceAdminController {

    private final PlaceService placeService;

    /**
     * [초기 데이터 구축용] 천안시 전체 장소 데이터를 카카오 API에서 가져와 DB에 저장합니다.
     * @return 성공 메시지
     */
    @PostMapping("/fetch-all-cheonan")
    public ResponseEntity<String> fetchAllPlacesForCheonan() {
        placeService.fetchAllPlacesForCheonan();
        return ResponseEntity.ok("천안시 전체 장소 데이터 저장을 시작했습니다. (완료까지 시간이 걸릴 수 있습니다)");
    }

    /**
     * [개별 업데이트용] 특정 장소의 정보를 최신화합니다.
     * @param placeId 우리 DB에 저장된 장소의 ID (PK)
     * @return 업데이트된 장소 정보
     */
    @PostMapping("/update/{placeId}")
    public ResponseEntity<PlaceResponseDto> updatePlace(@PathVariable Integer placeId) {
        PlaceResponseDto updatedPlace = placeService.updatePlace(placeId);
        return ResponseEntity.ok(updatedPlace);
    }
}