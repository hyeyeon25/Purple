package Purple.Purple.tmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapFeature {

    private String type;  // "Feature"

    private TmapGeometry geometry;

    private TmapProperties properties;
}
