package Purple.Purple.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Neighborhood, which acts as a container for Places.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Neighborhood")
public class Neighborhood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "neighborhood_id")
    private Integer id;

    @Column(name = "neighborhood_name", nullable = false, unique = true, length = 100)
    private String neighborhoodName;

    @Column(name = "neighborhood_latitude")
    private Double neighborhoodLatitude;

    @Column(name = "neighborhood_longitude")
    private Double neighborhoodLongitude;

    // Establishes the one-to-many relationship with Place.
    @OneToMany(mappedBy = "neighborhood")
    private List<Place> places = new ArrayList<>();
}