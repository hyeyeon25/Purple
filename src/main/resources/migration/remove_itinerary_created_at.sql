-- itinerary 테이블에서 itinerary_created_at 컬럼 제거
ALTER TABLE itinerary DROP COLUMN IF EXISTS itinerary_created_at;

