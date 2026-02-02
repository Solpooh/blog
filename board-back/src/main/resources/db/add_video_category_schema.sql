-- ============================================
-- Video 테이블 카테고리 컬럼 추가
-- 실행 방법: MySQL 클라이언트에서 직접 실행
-- ============================================

USE board;

-- 컬럼 추가
ALTER TABLE video
ADD COLUMN main_category VARCHAR(50) COMMENT '대분류 카테고리',
ADD COLUMN sub_category VARCHAR(50) COMMENT '소분류 카테고리';

-- 확인
DESC video;
