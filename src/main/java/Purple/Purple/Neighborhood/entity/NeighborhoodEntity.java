package Purple.Purple.Neighborhood.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "neighborhood")
@Getter @Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class NeighborhoodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "neighborhood_id")
    private Integer neighborhoodId;

    @Column(name = "neighborhood_name", nullable = false, unique = true)
    private String neighborhoodName;

    @Column(name = "neighborhood_latitude")
    private Double neighborhoodLatitude;

    @Column(name = "neighborhood_longitude")
    private Double neighborhoodLongitude;
}