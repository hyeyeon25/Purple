package Purple.Purple.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the User entity, minimized for the path management context.
 * It primarily serves to link an Itinerary to a user ID.
 */
@Entity
@Getter
@Setter
// Using backticks to avoid collision with the SQL reserved keyword 'User'.
@Table(name = "`User`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Establishes the one-to-many relationship with Itinerary.
    // When a User is deleted, all their Itineraries are also deleted.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Itinerary> itineraries = new ArrayList<>();
}