package Purple.Purple.domian;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // 기본 생성자와 getter/setter 생략 가능 (Lombok 쓰면 더 편함)
}
