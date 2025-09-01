# Claude Code 일관성 있는 요청 가이드

## 🎯 목적
- 각 단계별 구현 요청 시 일관된 품질과 구조 유지
- Claude가 프로젝트 현재 상태를 정확히 파악하여 적절한 구현 제공
- 단계별 완성도 체크 및 문서 업데이트 자동화

---

## 📋 현재 프로젝트 상태 (2025-08-31 기준)

### ✅ 완성된 단계
1. **기본 CRUD 게시판** (main 브랜치)
2. **타임리프 기본 문법** (thymeleaf-basic 브랜치)  
3. **타임리프 프래그먼트** (thymeleaf 브랜치)
4. **타임리프 Layout Dialect** (thymeleaf-dialect 브랜치)

### 📚 완성된 문서
- `THYMELEAF_BEGINNER_GUIDE.md` - 초보자용 타임리프 문법 가이드
- `THYMELEAF_TEMPLATE_EVOLUTION.md` - 3단계 템플릿 진화 과정
- `PRODUCTION_READY_CHECKLIST.md` - 실무 수준 개선 체크리스트
- `PRODUCTION_READY_ROADMAP.md` - 전체 학습 여정 가이드
- `lectures/10_VALIDATION.md` ~ `14_I18N_MESSAGES.md` - 각 강의별 상세 가이드

### 🎯 다음 구현 대기 중인 단계 (우선순위 순)
1. **10강: 데이터 검증 (Validation)** 
2. **11강: 전역 예외 처리 (Exception Handling)**
3. **12강: 수동 인증 시스템 (Manual Authentication)**  
4. **13강: Spring Security 도입 (Spring Security)**
5. **14강: 국제화 및 메시지 외부화 (i18n & Messages)**

---

## 🚀 요청 템플릿

### **기본 요청 형식**
```
[단계명] 구현해줘

참고: docs/request/PROJECT_STATUS.md
```

### **예시 요청들**

#### ✅ 권장 요청 방식
```
10강 Validation 구현해줘

참고: docs/request/PROJECT_STATUS.md
```

```
11강 전역 예외 처리 구현해줘  

참고: docs/request/PROJECT_STATUS.md
```

```
12강 수동 인증 시스템 구현해줘

참고: docs/request/PROJECT_STATUS.md
```

#### ❌ 비권장 요청 방식
```
검증 기능 만들어줘  (어떤 단계인지 불명확)
로그인 만들어줘     (수동인지 Spring Security인지 불명확)
```

---

## 🔄 Claude의 표준 구현 프로세스

### 1단계: 현재 상태 파악
- `docs/request/PROJECT_STATUS.md` 확인
- 관련 강의 문서 검토 (`docs/lectures/XX_TOPIC.md`)
- 베이스 브랜치 결정 (보통 최신 완성 브랜치)

### 2단계: 브랜치 생성 및 구현
```bash
# 브랜치 명명 규칙
10강 → validation
11강 → exception-handling  
12강 → manual-auth
13강 → spring-security
14강 → i18n-messages
```

### 3단계: 구현 내용
- 강의 문서에 명시된 핵심 코드 구현
- Spring 핵심 개념 (IoC/DI, AOP, MVC) 연결점 포함
- 실습 체크포인트 모든 항목 동작 확인

### 4단계: 테스트 및 문서화
- 애플리케이션 정상 실행 확인
- 주요 기능 동작 테스트
- 구현 과정에서 발견한 이슈나 개선사항 정리

### 5단계: 완성 및 상태 업데이트
- 브랜치 커밋 및 원격 푸시
- `docs/request/PROJECT_STATUS.md` 업데이트
- 다음 단계 준비 상태 확인

---

## 📝 Claude가 참조해야 할 핵심 정보

### 🎯 각 단계별 참조 문서
- **기본 정보**: `docs/request/PROJECT_STATUS.md` (필수)
- **강의 가이드**: `docs/lectures/XX_TOPIC.md` (해당 단계)
- **전체 로드맵**: `docs/lectures/PRODUCTION_READY_ROADMAP.md`
- **기술 스택**: `CLAUDE.md` (프로젝트 개요)

### 🔧 구현 시 고려사항
1. **일관된 코딩 스타일** - 기존 코드와 동일한 패턴 유지
2. **패키지 구조** - `io.goorm.board` 패키지 사용 (com.goorm 아님!)
3. **브랜치 전략** - 각 단계는 독립적인 브랜치로 관리
4. **Spring 버전** - Spring Boot 3.5.5, Java 21 기준
5. **의존성 관리** - build.gradle에 필요한 의존성만 최소 추가

### 🎪 강의 연결점
- AOP/IoC/DI 개념이 실제 코드에서 어떻게 적용되는지 명시
- 이전 단계와의 연결성 및 진화 과정 설명
- 실무 활용도 및 현업에서의 중요성 강조

---

## 🔄 단계 완성 후 업데이트 가이드

### Claude가 완성 후 해야 할 일
1. **PROJECT_STATUS.md 업데이트** - 완성된 단계 체크 표시
2. **다음 단계 준비** - 베이스 브랜치 정보 업데이트
3. **발견된 이슈** - 구현 과정에서 발견한 개선사항 기록
4. **테스트 결과** - 주요 기능 동작 확인 결과 기록

### 업데이트 예시
```markdown
### ✅ 완성된 단계 (업데이트됨)
4. **타임리프 Layout Dialect** (thymeleaf-dialect 브랜치)
5. **데이터 검증** (validation 브랜치) ⭐ NEW

### 🎯 다음 구현 대기 중인 단계
1. **11강: 전역 예외 처리** ← 다음 구현 대상
2. **12강: 수동 인증 시스템**
...

### 📍 현재 베이스 브랜치
- **최신 완성 브랜치**: validation
- **다음 구현 베이스**: validation 브랜치에서 시작
```

---

## 🎯 품질 보증 체크리스트

Claude가 각 단계 완성 시 확인해야 할 항목:

### ✅ 코드 품질
- [ ] 컴파일 에러 없음
- [ ] 애플리케이션 정상 실행
- [ ] 기존 기능 영향 없음
- [ ] 일관된 코딩 스타일

### ✅ 기능 완성도  
- [ ] 강의 문서의 모든 실습 체크포인트 통과
- [ ] 핵심 기능 정상 동작
- [ ] 에러 처리 적절히 구현
- [ ] 사용자 경험 고려

### ✅ Spring 개념 연결
- [ ] AOP/IoC/DI 개념이 코드에 명확히 드러남
- [ ] 이전 단계와의 연결성 유지
- [ ] 실무 패턴 적용

### ✅ 문서화
- [ ] 구현 과정 요약
- [ ] 주요 변경사항 기록
- [ ] 다음 단계 준비 상태 확인

---

## 🚨 주의사항

### DO (권장사항)
- ✅ 한 번에 하나의 단계만 구현
- ✅ 강의 문서를 정확히 따라 구현  
- ✅ 기존 브랜치 구조 및 코딩 스타일 유지
- ✅ 각 단계의 Spring 핵심 개념 강조
- ✅ 실습 체크포인트 모두 확인

### DON'T (주의사항)
- ❌ 여러 단계를 한 번에 구현하지 말 것
- ❌ 강의 문서 없이 임의로 구현하지 말 것
- ❌ 기존 브랜치나 코드 구조 변경하지 말 것
- ❌ 불필요한 추가 기능 구현하지 말 것
- ❌ 패키지 구조 변경하지 말 것 (io.goorm.board 유지)

---

**🎯 핵심**: 이 가이드를 따르면 일관된 품질과 구조로 각 단계를 완성할 수 있습니다!