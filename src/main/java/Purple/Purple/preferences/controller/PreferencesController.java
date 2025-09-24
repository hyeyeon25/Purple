package Purple.Purple.preferences.controller;

import Purple.Purple.preferences.dto.UserPreferenceRequest;
import Purple.Purple.preferences.dto.UserPreferenceResponse;
import Purple.Purple.preferences.service.PreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        UserPreferenceResponse response = preferencesService.savePreferences(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "성향 조회", description = "사용자의 성향을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성향 조회 성공"),
            @ApiResponse(responseCode = "404", description = "성향 정보 없음")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserPreferenceResponse> getPreferences(@PathVariable Long userId) {
        UserPreferenceResponse response = preferencesService.getPreferences(userId);
        return ResponseEntity.ok(response);
    }
}
