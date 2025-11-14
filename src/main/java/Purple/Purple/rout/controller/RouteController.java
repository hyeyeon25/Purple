package Purple.Purple.rout.controller;


import Purple.Purple.rout.dto.PlaceResponseDto;
import Purple.Purple.rout.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/route")
@Tag(name = "Route", description = "경로 최적화 API")
public class RouteController {
    private final RouteService routeService;
    
    public RouteController(@Qualifier("optimalRouteService") RouteService routeService) {
        this.routeService = routeService;
    }

    @Operation(summary = "최적 경로 계산",
            description = "DB의 장소들을 최단 거리 순서로 정렬한 상세 정보 리스트를 반환.")
    @GetMapping("/optimal")
    public ResponseEntity<List<PlaceResponseDto>> findOptimalRoute() {
        return ResponseEntity.ok(routeService.findOptimalRoute());
    }
}
