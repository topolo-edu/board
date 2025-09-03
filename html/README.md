# 06A-1 단계 - 순수 HTML+TailwindCSS 템플릿

이 폴더에는 수동 인증 시스템 구현을 위한 순수 HTML 템플릿들이 포함되어 있습니다.

## 파일 목록

### 1. `login.html`
- **목적**: 로그인 페이지
- **포함 요소**:
  - 아이디/비밀번호 입력 폼
  - 에러 메시지 표시 영역
  - 성공 메시지 표시 영역 (회원가입 완료 시)
  - 회원가입 페이지로의 링크

### 2. `signup.html`
- **목적**: 회원가입 페이지
- **포함 요소**:
  - 사용자 정보 입력 폼 (아이디, 비밀번호, 이메일, 닉네임)
  - 실시간 비밀번호 확인 검증
  - 유효성 검사 안내 메시지
  - 에러 메시지 표시 영역

### 3. `profile.html`
- **목적**: 사용자 프로필 관리 페이지
- **포함 요소**:
  - 기본 정보 수정 폼
  - 비밀번호 변경 섹션
  - 네비게이션 바 포함
  - 실시간 비밀번호 확인 검증

## 사용 방법

### 1. 개발 진행 순서
1. **엔티티 및 Repository 생성**
2. **Service 레이어 구현**
3. **Controller 구현**
4. **Thymeleaf 템플릿으로 변환**

### 2. HTML → Thymeleaf 변환 시 주요 변경점

#### 폼 액션 URL
```html
<!-- HTML -->
<form action="/auth/login" method="POST">

<!-- Thymeleaf -->
<form th:action="@{/auth/login}" method="POST">
```

#### 에러 메시지 표시
```html
<!-- HTML -->
<div id="error-message" class="hidden">

<!-- Thymeleaf -->
<div th:if="${error}" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
    <span th:text="${error}"></span>
</div>
```

#### 입력 필드 값 바인딩
```html
<!-- HTML -->
<input name="username" type="text">

<!-- Thymeleaf -->
<input th:field="*{username}" type="text">
```

#### 사용자 정보 표시
```html
<!-- HTML -->
<span class="text-gray-700">홍길동님</span>

<!-- Thymeleaf -->
<span class="text-gray-700" th:text="${session.user.displayName} + '님'"></span>
```

## 스타일 가이드

- **색상 체계**: Indigo를 기본 색상으로 사용
- **반응형**: TailwindCSS의 반응형 클래스 활용
- **접근성**: 라벨과 폼 필드 연결, 적절한 콘트라스트
- **사용자 경험**: 실시간 검증, 명확한 에러/성공 메시지

## 주의사항

1. **보안**: 실제 구현 시 CSRF 토큰, XSS 방어 등 보안 조치 필요
2. **검증**: 클라이언트 검증은 UX 향상용이며, 서버 사이드 검증 필수
3. **국제화**: 메시지는 추후 properties 파일로 분리 예정

---

**다음 단계**: `docs/lecture/06A-1. MANUAL_AUTH_BASIC_GUIDE.md` 문서를 참고하여 실제 구현을 진행하세요.