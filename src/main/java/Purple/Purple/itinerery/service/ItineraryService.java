package Purple.Purple.itinerery.service;

import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.itinerery.domain.ItineraryPlace;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.place.repository.PlaceRepository;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.repository.UserRepository;
import Purple.Purple.itinerery.dto.ItineraryCreateRequestDto;
import Purple.Purple.itinerery.dto.ItineraryDetailResponseDto;
import Purple.Purple.itinerery.dto.PlaceAddRequestDto;
import Purple.Purple.itinerery.repository.ItineraryRepository;
import Purple.Purple.itinerery.repository.ItineraryPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final ItineraryPlaceRepository itineraryPlaceRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    // 여행 계획(폴더) 생성
    @Transactional
    public Integer createItinerary(ItineraryCreateRequestDto requestDto, Long userId) {
        // 2. userId로 사용자를 찾도록 로직 수정 및 타입 변경
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));

        Itinerary itinerary = new Itinerary();
        itinerary.setUser(user); // UserPersonalInfo 객체를 설정
        itinerary.setItineraryTitle(requestDto.getItineraryTitle());
        itinerary.setDate(requestDto.getDate());
        itinerary.setItineraryGeneratedByAi(false);

        Itinerary savedItinerary = itineraryRepository.save(itinerary);
        return savedItinerary.getItineraryId();
    }

    // 여행 계획(폴더)에 장소 추가
    @Transactional
    public Integer addPlaceToItinerary(Integer itineraryId, PlaceAddRequestDto requestDto, Long userId) {
        // 3. 모든 User 타입을 UserPersonalInfo로 변경
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
        Itinerary itinerary = itineraryRepository.findByItineraryIdAndUser(itineraryId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없거나 소유자가 아닙니다. id=" + itineraryId));
        PlaceEntity place = placeRepository.findById(requestDto.getPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + requestDto.getPlaceId()));

        ItineraryPlace itineraryPlace = new ItineraryPlace();
        itineraryPlace.setItinerary(itinerary);
        itineraryPlace.setPlace(place);

        int nextOrder = itinerary.getItineraryPlaces().size() + 1;
        itineraryPlace.setVisitOrder(nextOrder);

        ItineraryPlace savedItineraryPlace = itineraryPlaceRepository.save(itineraryPlace);
        return savedItineraryPlace.getItineraryPlaceId();
    }

    // 여행 계획(폴더) 상세 조회
    @Transactional(readOnly = true)
    public ItineraryDetailResponseDto getItineraryDetails(Integer itineraryId, Long userId) {
        // 4. 상세 조회 시에도 사용자 권한을 확인하는 로직 추가
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
        Itinerary itinerary = itineraryRepository.findByItineraryIdAndUser(itineraryId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없거나 소유자가 아닙니다. id=" + itineraryId));

        return new ItineraryDetailResponseDto(itinerary);
    }

    // 여행 계획(폴더) 삭제
    @Transactional
    public void deleteItinerary(Integer itineraryId, Long userId) {
        // 5. 모든 User 타입을 UserPersonalInfo로 변경
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
        Itinerary itinerary = itineraryRepository.findByItineraryIdAndUser(itineraryId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없거나 소유자가 아닙니다. id=" + itineraryId));
        itineraryRepository.delete(itinerary);
    }
}
