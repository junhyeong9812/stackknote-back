-- StackNote 데이터베이스 초기화

-- UUID 확장 기능 활성화
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 시간대 설정
SET timezone = 'Asia/Seoul';-- StackNote 데이터베이스 초기화 스크립트

-- 사용자 생성 (이미 존재하는 경우 무시)
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'stacknote_user') THEN
      CREATE USER stacknote_user WITH PASSWORD 'stacknote_password';
   END IF;
END
$$;

-- 데이터베이스 생성 (이미 존재하는 경우 무시)
SELECT 'CREATE DATABASE stacknote OWNER stacknote_user'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'stacknote');

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE stacknote TO stacknote_user;

-- 확장 기능 설치 (UUID, 전문 검색 등)
\c stacknote;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 기본 스키마 설정
GRANT ALL ON SCHEMA public TO stacknote_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO stacknote_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO stacknote_user;

-- 기본 시간대 설정
SET timezone = 'Asia/Seoul';