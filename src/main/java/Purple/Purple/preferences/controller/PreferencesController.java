package Purple.Purple.preferences.controller;

import Purple.Purple.preferences.dto.UserPreferenceRequest;
import Purple.Purple.preferences.dto.UserPreferenceResponse;
import Purple.Purple.preferences.service.PreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferencesController {
    private final PreferencesService preferencesService;


    @Operation(summary = "성향 등록", description = "사용자의 성향을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성향 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<UserPreferenceResponse> savePreferences(
            @PathVariable Long userId,
            @RequestBody UserPreferenceRequest request) {
        try {
            // extrovertPreference 유효성 검증 (0-100 범위)
            if (request.getExtrovertPreference() < 0 || request.getExtrovertPreference() > 100) {
                log.error("성향 등록 실패 - userId: {}, extrovertPreference 범위 초과: {}",
                        userId, request.getExtrovertPreference());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            log.info("성향 등록 요청 - userId: {}, request: {}", userId, request);
            UserPreferenceResponse response = preferencesService.savePreferences(userId, request);
            log.info("성향 등록 성공 - userId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("성향 등록 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("성향 등록 중 예외 발생 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "성향 조회", description = "사용자의 성향을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성향 조회 성공"),
            @ApiResponse(responseCode = "404", description = "성향 정보 없음")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserPreferenceResponse> getPreferences(@PathVariable Long userId) {
        try {
            log.info("성향 조회 요청 - userId: {}", userId);
            UserPreferenceResponse response = preferencesService.getPreferences(userId);
            log.info("성향 조회 성공 - userId: {}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("성향 조회 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("성향 조회 중 예외 발생 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
