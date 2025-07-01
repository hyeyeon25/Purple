package Purple.Purple.controller;

import Purple.Purple.dto.ItineraryCreateRequestDto;
import Purple.Purple.dto.ItineraryDetailResponseDto;
import Purple.Purple.dto.PlaceAddRequestDto;
import Purple.Purple.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;

    // 여행 계획(폴더) 생성 API
    @PostMapping
    public ResponseEntity<Integer> createItinerary(@RequestBody ItineraryCreateRequestDto requestDto) {
        // TODO: 실제 사용 시에는 Spring Security의 Authentication 객체에서 사용자 ID를 가져와야 합니다.
        Long currentUserId = 1L; // 임시 사용자 ID
        Integer itineraryId = itineraryService.createItinerary(requestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryId);
    }

    // 여행 계획(폴더) 상세 조회 API
    @GetMapping("/{itineraryId}")
    public ResponseEntity<ItineraryDetailResponseDto> getItineraryDetails(@PathVariable Integer itineraryId) {
        ItineraryDetailResponseDto responseDto = itineraryService.getItineraryDetails(itineraryId);
        return ResponseEntity.ok(responseDto);
    }

    // 여행 계획(폴더)에 장소 추가 API
    @PostMapping("/{itineraryId}/places")
    public ResponseEntity<Integer> addPlaceToItinerary(
            @PathVariable Integer itineraryId,
            @RequestBody PlaceAddRequestDto requestDto) {
        // TODO: 실제 사용 시에는 Spring Security의 Authentication 객체에서 사용자 ID를 가져와야 합니다.
        Long currentUserId = 1L; // 임시 사용자 ID
        Integer itineraryPlaceId = itineraryService.addPlaceToItinerary(itineraryId, requestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryPlaceId);
    }

    // 여행 계획(폴더) 삭제 API
    @DeleteMapping("/{itineraryId}")
    public ResponseEntity<Void> deleteItinerary(@PathVariable Integer itineraryId) {
        // TODO: 실제 사용 시에는 Spring Security의 Authentication 객체에서 사용자 ID를 가져와야 합니다.
        Long currentUserId = 1L; // 임시 사용자 ID
        itineraryService.deleteItinerary(itineraryId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
