# 11강: 전역 예외 처리 (Global Exception Handling) - 에러도 우아하게!

## 🎯 학습 목표
- **@ControllerAdvice**를 통한 전역 예외 처리 구현
- 예외 처리가 **AOP의 실전 적용**임을 이해
- 사용자 친화적 에러 페이지 제공 방법 학습

---

## 🚨 현재 문제 상황

### "존재하지 않는 게시글에 접근해보세요!"

**문제점들**:
1. **존재하지 않는 게시글 접근** 시 500 에러 노출
2. **데이터베이스 연결 오류** 시 시스템 정보 노출
3. **사용자 친화적이지 않은** 기본 에러 페이지
4. **보안상 위험**: 서버 내부 정보 외부 노출

```java
// 현재 상태: 예외 처리 없음
@GetMapping("/posts/{id}")
public String show(@PathVariable Long id, Model model) {
    Post post = postService.findById(id);  // 없으면 Exception 발생!
    model.addAttribute("post", post);
    return "post/show";
}
```

**결과**: 사용자에게 무서운 500 에러 페이지가 그대로 노출 😱

---

## 💡 해결책: @ControllerAdvice

### 1단계: 커스텀 예외 클래스 생성

```java
// 비즈니스 예외 정의
public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }
}

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```

### 2단계: 전역 예외 처리기 생성

```java
@ControllerAdvice  // 🔑 모든 Controller의 예외를 가로채는 AOP
@Slf4j
public class GlobalExceptionHandler {
    
    // 데이터 없음 예외 처리
    @ExceptionHandler(DataNotFoundException.class)
    public String handleDataNotFound(DataNotFoundException e, Model model) {
        log.error("Data not found: {}", e.getMessage());
        model.addAttribute("error", "요청하신 데이터를 찾을 수 없습니다.");
        model.addAttribute("message", e.getMessage());
        return "error/404";  // 사용자 친화적 404 페이지
    }
    
    // 검증 실패 예외 처리
    @ExceptionHandler(ValidationException.class)
    public String handleValidation(ValidationException e, Model model) {
        log.warn("Validation failed: {}", e.getMessage());
        model.addAttribute("error", "입력 정보를 다시 확인해주세요.");
        model.addAttribute("message", e.getMessage());
        return "error/validation";
    }
    
    // 데이터베이스 예외 처리
    @ExceptionHandler(DataAccessException.class)
    public String handleDataAccess(DataAccessException e, Model model) {
        log.error("Database error occurred", e);
        model.addAttribute("error", "일시적인 서버 오류입니다. 잠시 후 다시 시도해주세요.");
        return "error/500";
    }
    
    // 모든 예외의 최종 처리
    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception e, Model model, HttpServletRequest request) {
        log.error("Unexpected error occurred at {}: ", request.getRequestURL(), e);
        model.addAttribute("error", "예상치 못한 오류가 발생했습니다.");
        return "error/500";
    }
}
```

### 3단계: Service에서 적절한 예외 발생

```java
@Service
public class PostService {
    
    public Post findById(Long id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));
    }
    
    public Post save(Post post) {
        // 비즈니스 룰 검증
        if (post.getTitle().contains("금지어")) {
            throw new ValidationException("제목에 부적절한 내용이 포함되어 있습니다.");
        }
        
        return postRepository.save(post);
    }
}
```

### 4단계: 사용자 친화적 에러 페이지 생성

```html
<!-- templates/error/404.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layouts/default}">
<head>
    <title>페이지를 찾을 수 없습니다</title>
</head>
<body>
    <div layout:fragment="content" class="text-center py-16">
        <div class="text-6xl mb-4">🔍</div>
        <h1 class="text-3xl font-bold mb-4">페이지를 찾을 수 없습니다</h1>
        
        <p th:text="${error}" class="text-gray-600 mb-4">
            요청하신 페이지를 찾을 수 없습니다.
        </p>
        
        <div th:if="${message}" class="bg-red-50 p-4 rounded mb-4">
            <p th:text="${message}" class="text-red-800"></p>
        </div>
        
        <div class="space-x-4">
            <a th:href="@{/}" class="btn btn-primary">홈으로 가기</a>
            <a th:href="@{/posts}" class="btn btn-secondary">게시판으로 가기</a>
        </div>
    </div>
</body>
</html>
```

---

## 🔗 Spring 핵심 개념 연결

### 🎯 AOP (Aspect Oriented Programming) 실전 적용

#### "@ControllerAdvice는 AOP의 완벽한 예시입니다!"

```java
// 기존 방식: 모든 Controller에 try-catch 중복
@Controller
public class PostController {
    public String show(@PathVariable Long id) {
        try {
            Post post = postService.findById(id);
            return "post/show";
        } catch (DataNotFoundException e) {
            // 예외 처리 로직 중복!
        } catch (Exception e) {
            // 예외 처리 로직 중복!
        }
    }
}

// AOP 방식: @ControllerAdvice가 모든 예외를 가로채서 처리
@ControllerAdvice  // 🎯 모든 Controller의 예외를 Advice로 처리
public class GlobalExceptionHandler {
    @ExceptionHandler(DataNotFoundException.class)
    public String handleDataNotFound(...) { ... }
}
```

**핵심 인사이트**:
- **횡단 관심사**: 예외 처리는 모든 Controller에서 필요
- **관심사 분리**: 비즈니스 로직과 예외 처리 로직 완전 분리
- **AOP Proxy**: Spring이 모든 Controller 메서드를 감싸서 예외 가로채기
- **선언적 처리**: 어노테이션으로 복잡한 예외 처리 자동화

### 🎯 Proxy 패턴 이해

```java
// Spring이 내부적으로 생성하는 Proxy 구조
PostController (원본) 
    ↓ Spring AOP가 감싸서
PostController$$EnhancerBySpringCGLIB (Proxy)
    ↓ 모든 메서드 호출을 가로채서
GlobalExceptionHandler로 예외 전달
```

**동작 과정**:
1. 클라이언트가 Controller 메서드 호출
2. **AOP Proxy**가 메서드 실행을 감시
3. 예외 발생 시 **@ControllerAdvice**가 가로채기
4. 예외 타입에 맞는 **@ExceptionHandler** 실행
5. 사용자 친화적 응답 반환

---

## 🛠️ 실습 체크포인트

### ✅ 예외 처리 동작 확인
1. **존재하지 않는 게시글 접근** → 친화적 404 페이지 확인
2. **데이터베이스 연결 끊기** → 500 에러 페이지 확인  
3. **잘못된 데이터 입력** → 검증 에러 페이지 확인

### ✅ AOP 개념 이해 확인
1. **Controller 코드에 try-catch 없음**에도 예외 처리되는 원리 이해
2. **@ControllerAdvice 하나**로 모든 Controller 예외 처리되는 AOP 개념 체감
3. **예외 타입별 다른 처리**가 어떻게 자동 매칭되는지 이해

### ✅ 사용자 경험 확인
1. **기술적 에러 메시지 숨김** → 사용자 친화적 메시지로 변환
2. **적절한 액션 버튼** 제공 (홈으로, 이전 페이지로)
3. **브랜딩 일관성** → 에러 페이지도 동일한 레이아웃 사용

---

## 🎪 강의 진행 팁

### 도입부 (5분)
```
"여러분, URL에 존재하지 않는 게시글 번호를 입력해보세요. 
/posts/99999 이런 식으로요. 어떤 페이지가 나오나요?"

→ 무서운 500 에러 페이지 시연
→ "이걸 고객이 본다면...?" 문제 인식시키기
```

### AOP 개념 연결 (10분)
```
"모든 Controller에서 try-catch를 써야 할까요?
100개 메서드가 있으면 100번 같은 예외 처리를 반복해야 할까요?

Spring의 @ControllerAdvice는 이 모든 예외를 한 곳에서 처리합니다.
지난 시간에 배운 검증(@Valid)처럼, 이것도 바로 AOP입니다!"
```

### 실무 연결 (10분)
```
"실무에서는 이런 예외 처리가 얼마나 중요할까요?
- 고객이 에러를 보면 → 서비스 신뢰도 하락
- 해커가 에러 정보를 보면 → 보안 위험
- 개발자가 에러를 놓치면 → 버그 발견 지연

@ControllerAdvice 하나로 이 모든 문제를 해결할 수 있습니다!"
```

### 마무리 (5분)
```
"이제 어떤 오류가 발생해도 사용자는 친화적인 페이지를 보게 됩니다.
하지만 아직도 누구나 게시글을 쓸 수 있고, 다른 사람 글도 지울 수 있어요.

다음 시간에는 '로그인' 기능을 직접 만들어보겠습니다!"
```

---

## 📚 핵심 키워드 정리

- **@ControllerAdvice**: 전역 예외 처리 AOP 컴포넌트
- **@ExceptionHandler**: 특정 예외 타입 처리 메서드
- **AOP 실전**: 횡단 관심사인 예외 처리를 Aspect로 분리
- **Proxy 패턴**: Spring이 Controller를 감싸서 예외 가로채기
- **관심사 분리**: 비즈니스 로직과 예외 처리 로직 완전 분리
- **사용자 경험**: 기술적 오류를 친화적 메시지로 변환

**🎯 핵심 메시지**: "예외 처리도 AOP로! 한 곳에서 모든 오류를 우아하게 처리하여 사용자 경험과 시스템 안정성을 동시에 확보합니다!"