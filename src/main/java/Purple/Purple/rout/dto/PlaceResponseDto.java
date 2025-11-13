package Purple.Purple.rout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceResponseDto {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String mapUrl;
    private String aiSummary;
}
