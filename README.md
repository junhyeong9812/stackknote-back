# StackNote Backend

> ⚡ **Notion-style Document Management System - Backend API**

노션 스타일의 문서 관리 시스템 백엔드 API입니다. Spring Boot를 기반으로 구축된 RESTful API 서버입니다.

## 📋 목차

- [기술 스택](#-기술-스택)
- [주요 기능](#-주요-기능)
- [시작하기](#-시작하기)
- [프로젝트 구조](#-프로젝트-구조)
- [API 문서](#-api-문서)
- [개발 가이드](#-개발-가이드)
- [배포](#-배포)

## 🛠️ 기술 스택

### Core
- **Java 21** - LTS 버전
- **Spring Boot 3.5.0** - 애플리케이션 프레임워크
- **Spring Security** - 인증 및 보안
- **Spring Data JPA** - 데이터 액세스 계층

### Database
- **PostgreSQL 16** - 운영 데이터베이스
- **H2** - 개발/테스트용 인메모리 데이터베이스

### Build & Tools
- **Gradle 8.5** - 빌드 도구
- **QueryDSL** - 타입 안전한 쿼리 작성
- **Lombok** - 보일러플레이트 코드 제거

### Additional Libraries
- **JWT (JJWT)** - JSON Web Token 인증
- **Flexmark** - 마크다운 파싱
- **SpringDoc OpenAPI** - API 문서 자동 생성

### Infrastructure
- **Docker & Docker Compose** - 컨테이너화
- **PostgreSQL Docker** - 데이터베이스 컨테이너

## ✨ 주요 기능

### 🔐 인증 & 보안
- **JWT 기반 인증** - 토큰 기반 인증 시스템
- **사용자 관리** - 회원가입, 로그인, 프로필 관리
- **권한 기반 접근 제어** - 역할별 API 접근 제한

### 📝 문서 관리
- **계층형 페이지 구조** - 워크스페이스 > 페이지 계층
- **마크다운 지원** - 실시간 마크다운 파싱 및 렌더링
- **버전 히스토리** - 문서 변경 이력 추적
- **전문 검색** - 제목, 내용 통합 검색

### 🏢 워크스페이스 관리
- **다중 워크스페이스** - 사용자별 여러 작업 공간
- **멤버 초대 시스템** - 이메일 기반 초대
- **권한 관리** - 읽기/쓰기/관리자 권한 설정

### 📁 파일 관리
- **이미지 업로드** - 프로젝트 내 images 폴더 관리
- **파일 메타데이터** - 파일 정보 및 접근 권한 관리
- **용량 제한** - 업로드 파일 크기 제한

## 🚀 시작하기

### 사전 요구사항
- **Java 21** 이상
- **Docker & Docker Compose** (PostgreSQL 사용 시)
- **Gradle 8.5** 이상

### 로컬 개발 환경 설정

#### 1. H2 데이터베이스 사용 (기본)

```bash
# 저장소 클론
git clone <repository-url>
cd stacknote-back

# 의존성 설치 및 빌드
./gradlew build

# 개발 서버 실행 (H2 사용)
./gradlew bootRun

# API 서버 접속
# http://localhost:8080/api
# H2 콘솔: http://localhost:8080/api/h2-console
```

#### 2. PostgreSQL 사용

```bash
# 환경 변수 설정
cp .env.example .env
# .env 파일 편집

# PostgreSQL만 실행
docker-compose up -d postgres

# Spring Boot를 PostgreSQL 프로필로 실행
./gradlew bootRun --args='--spring.profiles.active=prod'
```

#### 3. 전체 Docker 환경

```bash
# 전체 시스템 실행 (PostgreSQL + Spring Boot)
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서비스 중지
docker-compose down
```

### 환경 변수 설정

`.env` 파일을 생성하고 다음 변수들을 설정하세요:

```bash
# PostgreSQL Database
POSTGRES_DB=stacknote
POSTGRES_USER=stacknote_user
POSTGRES_PASSWORD=stacknote_password

# JWT Security
JWT_SECRET=stacknote-super-secret-jwt-key-for-production-at-least-256-bits-long
```

## 📁 프로젝트 구조

```
stacknote-back/
├── docker/                     # Docker 설정
│   ├── postgres/
│   │   ├── Dockerfile
│   │   └── init.sql
│   └── spring/
│       └── Dockerfile
├── src/main/java/com/stacknote/api/
│   ├── config/                 # 설정 클래스
│   │   ├── SecurityConfig.java
│   │   ├── JpaConfig.java
│   │   └── WebConfig.java
│   ├── controller/             # REST 컨트롤러
│   │   ├── AuthController.java
│   │   ├── WorkspaceController.java
│   │   ├── PageController.java
│   │   └── FileController.java
│   ├── service/                # 비즈니스 로직
│   │   ├── AuthService.java
│   │   ├── WorkspaceService.java
│   │   ├── PageService.java
│   │   └── FileService.java
│   ├── repository/             # 데이터 액세스
│   │   ├── UserRepository.java
│   │   ├── WorkspaceRepository.java
│   │   ├── PageRepository.java
│   │   └── FileRepository.java
│   ├── entity/                 # JPA 엔티티
│   │   ├── User.java
│   │   ├── Workspace.java
│   │   ├── Page.java
│   │   └── File.java
│   ├── dto/                    # 데이터 전송 객체
│   │   ├── request/
│   │   └── response/
│   ├── exception/              # 예외 처리
│   │   ├── GlobalExceptionHandler.java
│   │   └── custom/
│   └── util/                   # 유틸리티
│       ├── JwtUtil.java
│       └── MarkdownUtil.java
├── src/main/resources/
│   ├── application.yml         # 공통 설정
│   ├── application-dev.yml     # 개발환경 (H2)
│   └── application-prod.yml    # 운영환경 (PostgreSQL)
├── images/                     # 파일 업로드 저장소
├── .env                        # 환경 변수
├── docker-compose.yml          # Docker 구성
└── build.gradle               # 빌드 설정
```

## 📚 API 문서

### Swagger UI
개발 서버 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/api-docs

### 주요 엔드포인트

#### 인증 관련
```
POST /api/auth/register      # 회원가입
POST /api/auth/login         # 로그인
POST /api/auth/refresh       # 토큰 갱신
GET  /api/auth/profile       # 사용자 프로필
```

#### 워크스페이스 관리
```
GET    /api/workspaces           # 워크스페이스 목록
POST   /api/workspaces           # 워크스페이스 생성
GET    /api/workspaces/{id}      # 워크스페이스 상세
PUT    /api/workspaces/{id}      # 워크스페이스 수정
DELETE /api/workspaces/{id}      # 워크스페이스 삭제
```

#### 페이지 관리
```
GET    /api/workspaces/{workspaceId}/pages     # 페이지 목록
POST   /api/workspaces/{workspaceId}/pages     # 페이지 생성
GET    /api/pages/{id}                         # 페이지 상세
PUT    /api/pages/{id}                         # 페이지 수정
DELETE /api/pages/{id}                         # 페이지 삭제
```

#### 파일 관리
```
POST   /api/files/upload        # 파일 업로드
GET    /api/files/{id}          # 파일 다운로드
DELETE /api/files/{id}          # 파일 삭제
```

## 🔧 개발 가이드

### 코드 스타일

```bash
# QueryDSL Q클래스 생성
./gradlew compileJava

# 테스트 실행
./gradlew test

# 빌드
./gradlew build
```

### 새로운 엔티티 추가

1. **Entity 클래스 생성**
```java
@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
}
```

2. **Repository 인터페이스 생성**
```java
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameContaining(String name);
}
```

3. **Service 클래스 생성**
```java
@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
}
```

4. **Controller 클래스 생성**
```java
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;
    
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
}
```

### 데이터베이스 마이그레이션

개발 환경에서는 `ddl-auto: create-drop`을 사용하여 자동으로 테이블이 생성됩니다.
운영 환경에서는 `ddl-auto: validate`를 사용하며, 필요시 Flyway 등의 마이그레이션 도구를 추가할 수 있습니다.

## 🚢 배포

### Docker Compose 배포

```bash
# 운영 환경 실행
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs app
docker-compose logs postgres
```

### 수동 배포

```bash
# JAR 파일 빌드
./gradlew bootJar

# JAR 파일 실행
java -jar build/libs/stacknote-api.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

## 🗄️ 데이터베이스 관리

### 백업 및 복원

```bash
# 데이터베이스 백업
docker-compose exec postgres pg_dump -U stacknote_user stacknote > backup.sql

# 데이터베이스 복원
docker-compose exec -T postgres psql -U stacknote_user -d stacknote < backup.sql

# 데이터베이스 초기화
docker-compose down -v
docker-compose up -d postgres
```

### PostgreSQL 접속

```bash
# Docker 컨테이너를 통한 접속
docker-compose exec postgres psql -U stacknote_user -d stacknote

# 직접 접속 (로컬 PostgreSQL 설치 시)
psql -h localhost -p 5432 -U stacknote_user -d stacknote
```

## 📊 모니터링

### 헬스체크

```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/api/actuator/health

# 데이터베이스 연결 확인
curl http://localhost:8080/api/actuator/health/db
```

### 로그 관리

로그는 다음 레벨로 구성됩니다:
- **개발환경**: DEBUG (상세한 로그)
- **운영환경**: INFO (필요한 정보만)

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 🔗 관련 링크

- **프론트엔드 저장소**: [StackNote Frontend](../stacknote-front)
- **API 문서**: http://localhost:8080/api/swagger-ui.html
- **Spring Boot 문서**: https://spring.io/projects/spring-boot
- **PostgreSQL 문서**: https://www.postgresql.org/docs/