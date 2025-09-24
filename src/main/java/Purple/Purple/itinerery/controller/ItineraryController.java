package Purple.Purple.itinerery.controller;

import Purple.Purple.itinerery.dto.ItineraryCreateRequestDto;
import Purple.Purple.itinerery.dto.ItineraryDetailResponseDto;
import Purple.Purple.itinerery.dto.PlaceAddRequestDto;
import Purple.Purple.itinerery.service.ItineraryService;
import Purple.Purple.user.entity.UserPersonalInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;

    // 여행 계획(폴더) 생성 API
    @PostMapping
    public ResponseEntity<Integer> createItinerary(@RequestBody ItineraryCreateRequestDto requestDto,
                                                   @AuthenticationPrincipal UserPersonalInfo userDetails) {
        // @AuthenticationPrincipal을 통해 실제 로그인한 사용자의 정보를 가져옵니다.
        Long currentUserId = UserPersonalInfo.getId();
        Integer itineraryId = itineraryService.createItinerary(requestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryId);
    }

    // 여행 계획(폴더) 상세 조회 API
    @GetMapping("/{itineraryId}")
    public ResponseEntity<ItineraryDetailResponseDto> getItineraryDetails(@PathVariable Integer itineraryId,
                                                                          @AuthenticationPrincipal UserPersonalInfo userDetails) {
        Long currentUserId = UserPersonalInfo.getId();
        ItineraryDetailResponseDto responseDto = itineraryService.getItineraryDetails(itineraryId, currentUserId);
        return ResponseEntity.ok(responseDto);
    }

    // 여행 계획(폴더)에 장소 추가 API
    @PostMapping("/{itineraryId}/places")
    public ResponseEntity<Integer> addPlaceToItinerary(
            @PathVariable Integer itineraryId,
            @RequestBody PlaceAddRequestDto requestDto,
            @AuthenticationPrincipal UserPersonalInfo userDetails) {
        Long currentUserId = userDetails.getUserId();
        Integer itineraryPlaceId = itineraryService.addPlaceToItinerary(itineraryId, requestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(itineraryPlaceId);
    }

    // 여행 계획(폴더) 삭제 API
    @DeleteMapping("/{itineraryId}")
    public ResponseEntity<Void> deleteItinerary(@PathVariable Integer itineraryId,
                                                @AuthenticationPrincipal UserPersonalInfo userDetails) {
        Long currentUserId = userDetails.getUserId();
        itineraryService.deleteItinerary(itineraryId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}