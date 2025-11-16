-- itinerary 테이블에서 created_at 컬럼 제거
ALTER TABLE itinerary DROP COLUMN IF EXISTS created_at;

-- folder 테이블에서 folder_created_at 컬럼 제거
ALTER TABLE folder DROP COLUMN IF EXISTS folder_created_at;

-- Itinerary_Place 테이블에서 added_at 컬럼 제거
ALTER TABLE "Itinerary_Place" DROP COLUMN IF EXISTS added_at;
