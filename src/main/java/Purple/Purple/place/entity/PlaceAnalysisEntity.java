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
  @JoinColumn(name = "place_id") // DB에 place_id라는 FK 컬럼이 생김
  private PlaceEntity place;

  @Column(columnDefinition = "TEXT")
  private String reviewSummary; // CLOVA/GPT가 요약한 3~5줄 리뷰

  @Column(columnDefinition = "TEXT") // JSON 형태 문자열 저장
  private String keywords;

  @Column(name = "one_line_recommend")
  private String oneLineRecommend; // "조용한 분위기에서 공부하기 좋은 카페"

}
