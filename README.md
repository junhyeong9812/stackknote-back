# StackNote Frontend

> 🚀 **포트폴리오 & 스터디 노트 공유 플랫폼 - Frontend**

노션 스타일의 문서 관리 시스템으로 개인 포트폴리오와 공부 내용을 정리하고 공유할 수 있는 플랫폼입니다.

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [기술 스택](#-기술-스택)
- [주요 기능](#-주요-기능)
- [시작하기](#-시작하기)
- [프로젝트 구조](#-프로젝트-구조)
- [개발 가이드](#-개발-가이드)

## 🎯 프로젝트 개요

### 핵심 컨셉
개인의 **포트폴리오**와 **공부 내용**을 노션 스타일로 정리하고, 선택적으로 **공개/비공개** 설정하여 다른 사람들과 공유할 수 있는 플랫폼입니다.

### 주요 사용 시나리오
1. **개인 사용자**: 자신의 워크스페이스에서 포트폴리오/공부 내용 정리
2. **방문자 (게스트)**: 공개된 콘텐츠 열람 및 댓글 작성
3. **협업**: 게스트를 워크스페이스에 초대하여 제한적 협업

## 🛠️ 기술 스택

### Core Framework
- **Next.js 15.3.3** - React 프레임워크 (App Router)
- **React 19** - UI 라이브러리
- **TypeScript** - 타입 안전성
- **Tailwind CSS 4** - 유틸리티 CSS 프레임워크

### 노션 스타일 에디터
- **BlockNote** - 노션 스타일 블록 에디터
    - `@blocknote/core` `@blocknote/react` `@blocknote/mantine`
- **TipTap** - 고급 리치 텍스트 에디터 (백업/확장용)
    - 모든 TipTap extensions 포함 (마크다운, 테이블, 협업 등)

### 드래그 앤 드롭 & 레이아웃
- **@dnd-kit** - 현대적인 드래그 앤 드롭
    - `@dnd-kit/core` `@dnd-kit/sortable` `@dnd-kit/utilities` `@dnd-kit/modifiers`
- **react-grid-layout** - 노션 스타일 좌우 배치
- **react-resizable** - 리사이즈 가능한 컴포넌트

### 코드 & 마크다운
- **Syntax Highlighting**: `lowlight` `highlight.js` `react-syntax-highlighter`
- **Markdown**: `react-markdown` `remark` `remark-gfm` `remark-math`
- **Math**: `rehype-katex` (수식 렌더링)

### 상태 관리 & 데이터
- **Zustand** - 경량 클라이언트 상태 관리
- **TanStack Query** - 서버 상태 관리 및 캐싱
- **React Hook Form + Zod** - 폼 관리 및 검증

### UI 컴포넌트 & 애니메이션
- **Radix UI** - 접근성 좋은 헤드리스 UI 컴포넌트
    - Dialog, Dropdown, Popover, Tooltip, Tabs, Switch, Select 등
- **Framer Motion** - 부드러운 애니메이션
- **React Spring** - 마이크로 인터랙션
- **Lucide React** - 아이콘 라이브러리

### 파일 & 미디어
- **react-dropzone** - 드래그 앤 드롭 파일 업로드
- **react-image-crop** - 이미지 편집
- **react-image-gallery** - 이미지 갤러리

### 유틸리티 & 성능
- **lodash** - 유틸리티 함수
- **date-fns** - 날짜 처리
- **fuse.js** - 퍼지 검색
- **react-window** - 가상화 (성능 최적화)
- **uuid** - 고유 ID 생성

### 실시간 협업 (선택사항)
- **yjs** - 실시간 협업을 위한 CRDT
- **y-websocket** - WebSocket 기반 동기화

### 개발 도구
- **ESLint** - 코드 품질 관리
- **Prettier** - 코드 포맷팅
- **TypeScript Types** - 모든 라이브러리 타입 정의

## ✨ 주요 기능

### 🔐 인증 & 권한 시스템
- **메인 계정**: 개인 워크스페이스 소유자
- **게스트 계정**: 공개 콘텐츠 열람 및 댓글 작성
- **워크스페이스 초대**: 게스트를 제한적으로 초대

### 📝 노션 스타일 에디터
- **블록 기반 편집**: 텍스트, 헤딩, 리스트, 코드, 이미지 등
- **드래그 앤 드롭**: 블록 순서 변경 및 좌우 배치
- **마크다운 지원**: `/` 명령어로 블록 추가
- **실시간 미리보기**: 마크다운 실시간 변환
- **코드 하이라이팅**: 다양한 프로그래밍 언어 지원

### 🏢 워크스페이스 관리
- **개인 워크스페이스**: 메인 계정의 전용 공간
- **페이지 계층구조**: 노션과 동일한 트리 구조
- **공개/비공개 설정**: 페이지별 공개 범위 설정
- **게스트 접근 제어**: 초대된 게스트만 접근 가능

### 💬 소셜 기능
- **댓글 시스템**: 공개 페이지에 댓글 작성
- **게스트 피드백**: 포트폴리오에 대한 의견 수집
- **공유 기능**: 링크를 통한 페이지 공유

### 🔍 검색 & 탐색
- **전문 검색**: 제목, 내용, 태그 통합 검색
- **태그 시스템**: 카테고리별 콘텐츠 분류
- **최근 문서**: 사용자별 접근 이력
- **공개 피드**: 다른 사용자의 공개 콘텐츠 탐색

### 📁 미디어 관리
- **이미지 업로드**: 드래그 앤 드롭 지원
- **파일 첨부**: 다양한 파일 형식 지원
- **이미지 편집**: 크롭, 리사이즈 기능
- **미디어 갤러리**: 업로드된 파일 관리

## 🚀 시작하기

### 사전 요구사항
- **Node.js** 18.17 이상
- **npm** 또는 **yarn**

### 설치 및 실행
```bash
# 저장소 클론
git clone <repository-url>
cd stacknote-front

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev

# 브라우저에서 확인
# http://localhost:3000
```

### 환경 변수 설정
`.env.local` 파일을 생성하고 다음 변수들을 설정하세요:

```bash
# API 서버 URL
NEXT_PUBLIC_API_URL=http://localhost:8080/api

# 파일 업로드 제한 (MB)
NEXT_PUBLIC_MAX_FILE_SIZE=10

# 애플리케이션 환경
NEXT_PUBLIC_APP_ENV=development

# 게스트 계정 설정
NEXT_PUBLIC_GUEST_USERNAME=guest
NEXT_PUBLIC_GUEST_PASSWORD=guest123

# 실시간 협업 (선택사항)
NEXT_PUBLIC_WEBSOCKET_URL=ws://localhost:8080/ws
```

## 📁 프로젝트 구조

```
stacknote-front/
├── app/                          # Next.js App Router
│   ├── (auth)/                  # 인증 관련 페이지
│   │   ├── login/
│   │   ├── register/
│   │   └── guest/
│   ├── (main)/                  # 메인 애플리케이션
│   │   ├── dashboard/           # 대시보드 (공개 피드)
│   │   ├── workspace/           # 워크스페이스
│   │   │   └── [workspaceId]/
│   │   │       ├── page/
│   │   │       │   └── [pageId]/
│   │   │       ├── settings/
│   │   │       └── members/
│   │   └── public/              # 공개 페이지 (게스트용)
│   │       └── [username]/
│   │           └── [pageId]/
│   ├── api/                     # API 라우트
│   │   ├── upload/
│   │   ├── auth/
│   │   └── pages/
│   ├── globals.css
│   ├── layout.tsx
│   └── page.tsx
├── components/                   # 재사용 컴포넌트
│   ├── ui/                      # 기본 UI (Radix + Tailwind)
│   │   ├── button.tsx
│   │   ├── input.tsx
│   │   ├── dialog.tsx
│   │   ├── dropdown-menu.tsx
│   │   └── ...
│   ├── editor/                  # 에디터 관련
│   │   ├── blocknote-editor.tsx
│   │   ├── tiptap-editor.tsx
│   │   ├── block-renderer.tsx
│   │   ├── drag-drop-wrapper.tsx
│   │   └── syntax-highlighter.tsx
│   ├── workspace/               # 워크스페이스 관련
│   │   ├── workspace-sidebar.tsx
│   │   ├── page-tree.tsx
│   │   ├── member-list.tsx
│   │   └── guest-controls.tsx
│   ├── layout/                  # 레이아웃
│   │   ├── main-sidebar.tsx
│   │   ├── header.tsx
│   │   ├── navigation.tsx
│   │   └── breadcrumb.tsx
│   ├── auth/                    # 인증 관련
│   │   ├── login-form.tsx
│   │   ├── register-form.tsx
│   │   └── guest-login.tsx
│   ├── comments/                # 댓글 시스템
│   │   ├── comment-list.tsx
│   │   ├── comment-form.tsx
│   │   └── comment-item.tsx
│   └── media/                   # 미디어 관련
│       ├── image-upload.tsx
│       ├── file-drop-zone.tsx
│       └── media-gallery.tsx
├── lib/                         # 유틸리티 & 설정
│   ├── api/                     # API 클라이언트
│   │   ├── client.ts
│   │   ├── auth.ts
│   │   ├── pages.ts
│   │   ├── workspace.ts
│   │   └── comments.ts
│   ├── stores/                  # Zustand 스토어
│   │   ├── auth-store.ts
│   │   ├── workspace-store.ts
│   │   ├── editor-store.ts
│   │   └── ui-store.ts
│   ├── hooks/                   # 커스텀 훅
│   │   ├── use-auth.ts
│   │   ├── use-editor.ts
│   │   ├── use-drag-drop.ts
│   │   ├── use-file-upload.ts
│   │   └── use-realtime.ts
│   ├── utils/                   # 헬퍼 함수
│   │   ├── cn.ts
│   │   ├── editor-utils.ts
│   │   ├── file-utils.ts
│   │   ├── auth-utils.ts
│   │   └── date-utils.ts
│   └── config/                  # 설정
│       ├── editor-config.ts
│       ├── api-config.ts
│       └── auth-config.ts
├── types/                       # TypeScript 타입
│   ├── auth.ts
│   ├── workspace.ts
│   ├── editor.ts
│   ├── comment.ts
│   └── api.ts
└── public/                      # 정적 파일
    ├── icons/
    ├── images/
    └── sample-data/
```

## 🔧 개발 가이드

### 개발 흐름
1. **UI 컴포넌트 시스템** 구축 (Radix + Tailwind)
2. **인증 시스템** 구현 (메인/게스트 계정)
3. **블록 에디터** 구현 (BlockNote 기반)
4. **워크스페이스** 시스템 구축
5. **드래그 앤 드롭** 및 레이아웃 시스템
6. **파일 업로드** 및 미디어 관리
7. **댓글 시스템** 구현
8. **검색 및 공개 피드** 기능
9. **실시간 협업** (선택사항)

### 코드 스타일
```bash
# 린트 검사
npm run lint

# 타입 검사
npm run type-check

# 빌드 테스트
npm run build
```

### 권한 시스템 설계
```typescript
// 권한 레벨
enum Permission {
  OWNER = 'owner',      // 워크스페이스 소유자
  EDITOR = 'editor',    // 편집 권한
  VIEWER = 'viewer',    // 읽기 전용
  GUEST = 'guest'       // 게스트 (공개 콘텐츠만)
}

// 페이지 공개 설정
enum PageVisibility {
  PRIVATE = 'private',  // 소유자만
  INVITED = 'invited',  // 초대된 사람만
  PUBLIC = 'public'     // 모든 사람
}
```

## 🎨 디자인 시스템

### 컬러 팔레트
- **Primary**: 노션 스타일 그레이 계열
- **Accent**: 포인트 컬러 (파란색 계열)
- **Success/Warning/Error**: 시스템 상태 색상

### 컴포넌트 원칙
1. **접근성**: Radix UI 기반 접근성 보장
2. **일관성**: 디자인 토큰 기반 일관된 스타일
3. **반응형**: 모바일 퍼스트 디자인
4. **다크모드**: 다크/라이트 테마 지원

## 📚 참고 자료

### 핵심 라이브러리 문서
- [BlockNote](https://www.blocknotejs.org/) - 노션 스타일 에디터
- [TipTap](https://tiptap.dev/) - 리치 텍스트 에디터
- [dnd-kit](https://dndkit.com/) - 드래그 앤 드롭
- [Radix UI](https://www.radix-ui.com/) - 헤드리스 UI
- [Framer Motion](https://www.framer.com/motion/) - 애니메이션

### 기술 스택 문서
- [Next.js 15](https://nextjs.org/docs)
- [React 19](https://react.dev/)
- [Tailwind CSS 4](https://tailwindcss.com/docs)
- [Zustand](https://zustand-demo.pmnd.rs/)
- [TanStack Query](https://tanstack.com/query)

## 🔗 관련 링크
- **백엔드 저장소**: [StackNote Backend](../stacknote-back)
- **API 문서**: http://localhost:8080/api/swagger-ui.html
