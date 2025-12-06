package Purple.Purple.tmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapGeometry {

    private String type;  // "Point" 또는 "LineString"

    private Object coordinates;  // Point: [x, y], LineString: [[x1, y1], [x2, y2], ...]
}
