package Purple.Purple.domian;

import jakarta.persistence.*;

@Entity
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String category;
    private String location;
    private boolean recommended;

    // getter/setter 생략 가능 (Lombok 쓰면 더 간단해짐)
}
