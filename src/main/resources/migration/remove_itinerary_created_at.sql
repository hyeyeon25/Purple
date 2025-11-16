-- ============================================
-- 컬럼 제거 마이그레이션 스크립트
-- 실행 방법: PostgreSQL에서 직접 실행하거나 애플리케이션 재시작 전에 실행
-- ============================================

-- folder 테이블에서 folder_created_at 컬럼 제거 (NOT NULL 제약조건 때문에 우선 실행)
ALTER TABLE folder DROP COLUMN IF EXISTS folder_created_at;

-- itinerary 테이블에서 created_at 컬럼 제거
ALTER TABLE itinerary DROP COLUMN IF EXISTS created_at;

-- Itinerary_Place 테이블에서 added_at 컬럼 제거
ALTER TABLE itinerary_place DROP COLUMN IF EXISTS added_at;
