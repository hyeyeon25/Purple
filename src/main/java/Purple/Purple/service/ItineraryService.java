package Purple.Purple.service;

import Purple.Purple.domain.Itinerary;
import Purple.Purple.domain.ItineraryPlace;
import Purple.Purple.domain.Place;
import Purple.Purple.domain.User;
import Purple.Purple.dto.ItineraryCreateRequestDto;
import Purple.Purple.dto.ItineraryDetailResponseDto;
import Purple.Purple.dto.PlaceAddRequestDto;
import Purple.Purple.repository.ItineraryRepository;
import Purple.Purple.repository.ItineraryPlaceRepository;
import Purple.Purple.repository.PlaceRepository;
import Purple.Purple.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성해주는 Lombok 어노테이션
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final ItineraryPlaceRepository itineraryPlaceRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    // 여행 계획(폴더) 생성
    @Transactional
    public Integer createItinerary(ItineraryCreateRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));

        Itinerary itinerary = new Itinerary();
        itinerary.setUser(user);
        itinerary.setItineraryTitle(requestDto.getItineraryTitle());
        itinerary.setDate(requestDto.getDate());
        itinerary.setItineraryGeneratedByAi(false); // 사용자가 직접 생성

        Itinerary savedItinerary = itineraryRepository.save(itinerary);
        return savedItinerary.getItineraryId();
    }

    // 여행 계획(폴더)에 장소 추가
    @Transactional
    public Integer addPlaceToItinerary(Integer itineraryId, PlaceAddRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
        Itinerary itinerary = itineraryRepository.findByItineraryIdAndUser(itineraryId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없거나 소유자가 아닙니다. id=" + itineraryId));
        Place place = placeRepository.findById(requestDto.getPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id=" + requestDto.getPlaceId()));

        ItineraryPlace itineraryPlace = new ItineraryPlace();
        itineraryPlace.setItinerary(itinerary);
        itineraryPlace.setPlace(place);

        // 방문 순서는 현재 담긴 장소 수 + 1 로 자동 설정
        int nextOrder = itinerary.getItineraryPlaces().size() + 1;
        itineraryPlace.setVisitOrder(nextOrder);

        ItineraryPlace savedItineraryPlace = itineraryPlaceRepository.save(itineraryPlace);
        return savedItineraryPlace.getItineraryPlaceId();
    }

    // 여행 계획(폴더) 상세 조회
    @Transactional(readOnly = true)
    public ItineraryDetailResponseDto getItineraryDetails(Integer itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다. id=" + itineraryId));
        return new ItineraryDetailResponseDto(itinerary);
    }

    // 여행 계획(폴더) 삭제
    @Transactional
    public void deleteItinerary(Integer itineraryId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
        Itinerary itinerary = itineraryRepository.findByItineraryIdAndUser(itineraryId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없거나 소유자가 아닙니다. id=" + itineraryId));
        itineraryRepository.delete(itinerary);
    }
}
