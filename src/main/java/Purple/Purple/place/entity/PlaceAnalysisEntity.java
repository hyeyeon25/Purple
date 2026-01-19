package Purple.Purple.place.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "place_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceAnalysisEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 분석 데이터 고유 ID

  //기존 PlaceEntity와 1:1 연결
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  private PlaceEntity place;

  @Column(columnDefinition = "TEXT")
  private String reviewSummary; //GPT 요약 리뷰

  @Column(columnDefinition = "TEXT") // JSON 형태 문자열 저장
  private String keywords;

  @Column(name = "one_line_recommend")
  private String oneLineRecommend;

}
