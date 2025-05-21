package Purple.Purple.controller;

import Purple.Purple.dto.ScheduleRequest;
import Purple.Purple.dto.ScheduleResponse;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

@Tag(name = "Schedule", description = "일정 CRUD API")
@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private final Map<Long, ScheduleResponse> scheduleMap = new HashMap<>();
    private Long idCounter = 1L;

    @Operation(summary = "일정 생성", description = "일정을 새로 등록합니다.")
    @PostMapping
    public ScheduleResponse create(@RequestBody ScheduleRequest request) {
        ScheduleResponse response = new ScheduleResponse();
        response.id = idCounter++;
        response.title = request.title;
        response.description = request.description;
        response.startDate = request.startDate;
        response.endDate = request.endDate;
        scheduleMap.put(response.id, response);
        return response;
    }

    @Operation(summary = "일정 조회", description = "ID로 단일 일정을 조회합니다.")
    @GetMapping("/{id}")
    public ScheduleResponse get(@PathVariable Long id) {
        return scheduleMap.get(id);
    }

    @Operation(summary = "일정 목록 조회", description = "등록된 전체 일정을 조회합니다.")
    @GetMapping
    public List<ScheduleResponse> getAll() {
        return new ArrayList<>(scheduleMap.values());
    }

    @Operation(summary = "일정 수정", description = "일정 내용을 수정합니다.")
    @PutMapping("/{id}")
    public ScheduleResponse update(@PathVariable Long id, @RequestBody ScheduleRequest request) {
        ScheduleResponse response = scheduleMap.get(id);
        if (response != null) {
            response.title = request.title;
            response.description = request.description;
            response.startDate = request.startDate;
            response.endDate = request.endDate;
        }
        return response;
    }

    @Operation(summary = "일정 삭제", description = "해당 ID의 일정을 삭제합니다.")
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        scheduleMap.remove(id);
        return Map.of("message", "일정 삭제 완료");
    }
}
