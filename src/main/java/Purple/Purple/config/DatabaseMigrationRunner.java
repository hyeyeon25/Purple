package Purple.Purple.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 애플리케이션 시작 시 데이터베이스 마이그레이션을 수행하는 Runner
 * 컬럼 제거 등의 스키마 변경을 자동으로 실행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner {

	private final JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void runMigrations() {
		try {
			log.info("데이터베이스 마이그레이션 시작...");
			
			// 마이그레이션 SQL 파일 읽기
			ClassPathResource resource = new ClassPathResource("migration/remove_itinerary_created_at.sql");
			
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
				
				String sql = reader.lines()
						.filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("--"))
						.collect(Collectors.joining("\n"));
				
				// 세미콜론으로 구분된 각 SQL 문 실행
				String[] statements = sql.split(";");
				
				for (String statement : statements) {
					statement = statement.trim();
					if (!statement.isEmpty()) {
						try {
							log.info("실행 중: {}", statement.substring(0, Math.min(50, statement.length())) + "...");
							jdbcTemplate.execute(statement);
							log.info("✓ 실행 완료");
						} catch (Exception e) {
							// IF EXISTS를 사용했으므로 컬럼이 없어도 에러는 무시
							if (e.getMessage() != null && e.getMessage().contains("does not exist")) {
								log.debug("컬럼이 이미 존재하지 않음 (무시): {}", e.getMessage());
							} else {
								log.warn("마이그레이션 실행 중 경고 (계속 진행): {}", e.getMessage());
							}
						}
					}
				}
			}
			
			log.info("데이터베이스 마이그레이션 완료");
			
		} catch (Exception e) {
			log.error("데이터베이스 마이그레이션 중 오류 발생: {}", e.getMessage(), e);
			log.warn("서버는 정상적으로 시작되었으나, 마이그레이션에 실패했습니다.");
		}
	}
}

