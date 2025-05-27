import Purple.Purple.dto.PlaceRequest;
import Purple.Purple.dto.PlaceResponse;
import Purple.Purple.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Place", description = "장소 관리 API")
@RestController
@RequestMapping("/places")
public class PlaceController {

    @Autowired
    private PlaceService placeService;

    @Operation(summary = "장소 생성")
    @PostMapping
    public PlaceResponse create(@RequestBody PlaceRequest request) {
        return placeService.create(request);
    }

    @Operation(summary = "장소 조회")
    @GetMapping("/{id}")
    public PlaceResponse get(@PathVariable Long id) {
        return placeService.get(id);
    }

    @Operation(summary = "장소 목록 조회")
    @GetMapping
    public List<PlaceResponse> getAll() {
        return placeService.getAll();
    }

    @Operation(summary = "장소 수정")
    @PutMapping("/{id}")
    public PlaceResponse update(@PathVariable Long id, @RequestBody PlaceRequest request) {
        return placeService.update(id, request);
    }

    @Operation(summary = "장소 삭제")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        placeService.delete(id);
    }
}
