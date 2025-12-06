package Purple.Purple.tmap.dto;

public interface TmapWalkingApiClient {

    /**
     * 두 지점 간 도보 경로를 조회합니다.
     *
     * @param startX 출발지 경도
     * @param startY 출발지 위도
     * @param endX 도착지 경도
     * @param endY 도착지 위도
     * @return 도보 경로 정보
     */
    TmapPedestrianResponse getPedestrianRoute(
        Double startX,
        Double startY,
        Double endX,
        Double endY
    );

    /**
     * 경유지를 포함한 도보 경로를 조회합니다.
     *
     * @param startX 출발지 경도
     * @param startY 출발지 위도
     * @param endX 도착지 경도
     * @param endY 도착지 위도
     * @param passList 경유지 좌표 문자열 (예: "127.111,37.111_127.222,37.222")
     * @return 도보 경로 정보
     */
    TmapPedestrianResponse getPedestrianRouteWithWaypoints(
        Double startX,
        Double startY,
        Double endX,
        Double endY,
        String passList
    );
}
