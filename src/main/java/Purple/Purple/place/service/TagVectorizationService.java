package Purple.Purple.place.service;

import Purple.Purple.place.dto.BatchVectorizationResponseDto;
import Purple.Purple.place.dto.TagVectorizationRequestDto;
import Purple.Purple.place.dto.TagVectorizationResponseDto;

public interface TagVectorizationService {

    /**
     * 단일 장소의 태그를 벡터화하여 저장
     *
     * @param placeId 장소 ID
     * @param requestDto 벡터화 요청 (태그 리스트, 덮어쓰기 여부)
     * @return 벡터화 결과
     */
    TagVectorizationResponseDto vectorizeSinglePlace(Integer placeId, TagVectorizationRequestDto requestDto);

    /**
     * 모든 장소의 태그를 벡터화하여 저장 (배치 처리)
     *
     * @return 배치 처리 결과
     */
    BatchVectorizationResponseDto vectorizeAllPlaces();
}