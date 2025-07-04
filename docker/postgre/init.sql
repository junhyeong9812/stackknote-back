-- StackNote 데이터베이스 초기화 스크립트
-- 데이터베이스는 POSTGRES_DB 환경변수로 자동 생성됨

-- 확장 기능 설치
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 기본 시간대 설정
SET timezone = 'Asia/Seoul';

-- 스키마 권한 설정 (미래의 테이블들을 위해)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO stacknote_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO stacknote_user;