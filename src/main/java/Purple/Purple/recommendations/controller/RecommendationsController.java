package Purple.Purple.recommendations.controller;

import Purple.Purple.recommendations.dto.BestNeighborhoodDto;
import Purple.Purple.recommendations.dto.TopNeighborhoodsResponseDto;
import Purple.Purple.recommendations.service.RecommendationsService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // 모든 API에 인증 필요 설정
public class RecommendationsController {

    private final RecommendationsService recommendationsService;

    @GetMapping("/region/{region}/top")
    public ResponseEntity<TopNeighborhoodsResponseDto> getTop3Recommendations(
            @Parameter(description = "지역 이름 (예: 천안시)") @PathVariable String region,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // UserPrincipal 객체에서 사용자 ID를 꺼내 서비스에 전달
        // TopNeighborhoodsResponseDto response = recommendationsService.getTop3Recommendations(region, userPrincipal.getId());
        // return ResponseEntity.ok(response);
        return null;
    }

    @GetMapping("/region/{region}/best")
    public ResponseEntity<BestNeighborhoodDto> getBestRecommendation(
            @Parameter(description = "지역 이름 (예: 천안시)") @PathVariable String region,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
//        BestNeighborhoodDto response = recommendationsService.getBestRecommendation(region, userPrincipal.getId());
//        return ResponseEntity.ok(response);
        return null;
    }
}