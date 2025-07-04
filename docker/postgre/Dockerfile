# FROM postgres:16-alpine
#
# # 한국어 로케일 설정
# RUN apk add --no-cache musl-locales musl-locales-lang
# ENV LANG=ko_KR.UTF-8
# ENV LC_ALL=ko_KR.UTF-8
# ENV TZ=Asia/Seoul
#
# # PostgreSQL 설정 파일들 복사
# COPY postgresql.conf /etc/postgresql/postgresql.conf
# COPY pg_hba.conf /etc/postgresql/pg_hba.conf
# COPY init.sql /docker-entrypoint-initdb.d/
#
# # 포트 노출
# EXPOSE 5432
#
# # PostgreSQL 시작 명령어
# CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf", "-c", "hba_file=/etc/postgresql/pg_hba.conf"]
FROM postgres:16-alpine

# 한국어 로케일 설정
RUN apk add --no-cache musl-locales musl-locales-lang
ENV LANG=ko_KR.UTF-8
ENV LC_ALL=ko_KR.UTF-8
ENV TZ=Asia/Seoul

# 초기화 스크립트만 복사
COPY init.sql /docker-entrypoint-initdb.d/

# 포트 노출
EXPOSE 5432

# PostgreSQL 기본 명령어 사용 (커스텀 설정 파일 제거)
CMD ["postgres"]