# 10강: 데이터 검증 (Validation) - 사용자 입력을 믿지 마세요!

## 🎯 학습 목표
- Bean Validation을 통한 데이터 검증 구현
- 검증 로직이 **횡단 관심사**임을 이해 (AOP 개념 도입)
- 타임리프에서 검증 오류 표시 방법 학습

---

## 🚨 현재 문제 상황

### "게시글을 아무것도 입력하지 않고 등록해보세요!"

**문제점들**:
1. **빈 제목**으로 게시글 등록 가능
2. **빈 내용**으로 게시글 등록 가능  
3. **너무 긴 텍스트** 입력 시 데이터베이스 오류
4. **악성 스크립트** 삽입 위험 (XSS)

```html
<!-- 현재 상태: 아무 검증 없음 -->
<input type="text" name="title" />  <!-- 빈 값도 OK? -->
<textarea name="content"></textarea>  <!-- 빈 값도 OK? -->
```

---

## 💡 해결책: Bean Validation

### 1단계: Entity에 검증 어노테이션 추가

```java
@Entity
public class Post {
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 200, message = "제목은 1~200자로 입력해주세요")
    private String title;
    
    @NotBlank(message = "작성자는 필수입니다")
    @Size(min = 2, max = 50, message = "작성자명은 2~50자로 입력해주세요")
    private String author;
    
    @NotBlank(message = "내용은 필수입니다")
    @Size(min = 10, max = 4000, message = "내용은 10~4000자로 입력해주세요")
    private String content;
    
    // getters/setters...
}
```

### 2단계: Controller에 @Valid 적용

```java
@Controller
public class PostController {
    
    @PostMapping("/posts")
    public String save(@Valid @ModelAttribute Post post, 
                      BindingResult bindingResult,
                      Model model) {
        
        // 검증 오류가 있으면 폼으로 다시 이동
        if (bindingResult.hasErrors()) {
            return "post/form";  // 에러 메시지와 함께 폼 다시 표시
        }
        
        // 검증 통과 시에만 저장
        postService.save(post);
        return "redirect:/posts";
    }
}
```

### 3단계: 타임리프에서 에러 표시

```html
<form th:action="@{/posts}" th:object="${post}" method="post">
    <!-- 제목 입력 -->
    <div>
        <label for="title">제목 *</label>
        <input type="text" th:field="*{title}" 
               th:class="${#fields.hasErrors('title')} ? 'error-input' : ''">
        
        <!-- 제목 검증 오류 표시 -->
        <div th:if="${#fields.hasErrors('title')}" class="error-message">
            <span th:each="error : ${#fields.errors('title')}" th:text="${error}"></span>
        </div>
    </div>
    
    <!-- 내용 입력 -->
    <div>
        <label for="content">내용 *</label>
        <textarea th:field="*{content}"
                  th:class="${#fields.hasErrors('content')} ? 'error-input' : ''"></textarea>
                  
        <!-- 내용 검증 오류 표시 -->
        <div th:if="${#fields.hasErrors('content')}" class="error-message">
            <span th:each="error : ${#fields.errors('content')}" th:text="${error}"></span>
        </div>
    </div>
    
    <button type="submit">저장</button>
</form>
```

---

## 🔗 Spring 핵심 개념 연결

### 🎯 AOP (Aspect Oriented Programming) 개념 도입

#### "검증 로직은 횡단 관심사입니다!"

```java
// 모든 컨트롤러 메서드에서 검증이 필요
@PostMapping("/posts")
public String savePost(@Valid Post post, BindingResult result) { ... }

@PostMapping("/users")  
public String saveUser(@Valid User user, BindingResult result) { ... }

@PostMapping("/comments")
public String saveComment(@Valid Comment comment, BindingResult result) { ... }
```

**핵심 인사이트**: 
- 검증 로직은 **모든 엔티티**에 공통으로 적용
- **비즈니스 로직과 분리**되어 처리  
- **@Valid 어노테이션 하나**로 모든 검증 실행
- 이것이 바로 **AOP의 기본 개념**!

### 🎯 DI (Dependency Injection) 활용

```java
// Validator 인터페이스를 구현한 Bean들이 자동 주입
@Component
public class CustomPostValidator implements Validator {
    
    @Override
    public boolean supports(Class<?> clazz) {
        return Post.class.equals(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        Post post = (Post) target;
        
        // 커스텀 검증 로직
        if (post.getTitle().contains("금지어")) {
            errors.rejectValue("title", "forbidden.word", "금지된 단어입니다");
        }
    }
}

// Controller에서 DI로 주입받아 사용
@Controller
public class PostController {
    
    private final CustomPostValidator customValidator;
    
    // 생성자 주입 (DI)
    public PostController(CustomPostValidator customValidator) {
        this.customValidator = customValidator;
    }
}
```

---

## 🛠️ 실습 체크포인트

### ✅ 기본 검증 동작 확인
1. 제목 없이 등록 시도 → "제목은 필수입니다" 메시지 확인
2. 너무 긴 제목 입력 → "제목은 1~200자로..." 메시지 확인
3. 검증 통과 시 정상 등록 확인

### ✅ 사용자 경험 확인  
1. 에러 발생 시 **입력했던 내용 유지**되는지 확인
2. 에러 메시지가 **사용자 친화적**인지 확인
3. 여러 필드에 오류가 있을 때 **모든 오류 표시**되는지 확인

### ✅ AOP 개념 이해 확인
1. `@Valid` 어노테이션 **하나로 모든 검증 실행**되는 원리 이해
2. **비즈니스 로직과 검증 로직이 분리**되어 있음을 인식
3. 다른 엔티티에도 **동일한 패턴 적용** 가능함을 이해

---

## 🎪 강의 진행 팁

### 도입부 (5분)
```
"여러분, 지금까지 만든 게시판에서 제목을 아무것도 입력하지 않고 
등록 버튼을 눌러보세요. 어떻게 될까요?"

→ 실제 시연 후 문제점 인식시키기
```

### 개념 연결 (10분)  
```
"모든 입력 폼에서 검증이 필요하죠? 회원가입, 댓글 작성, 상품 등록...
이런 검증 로직을 매번 if문으로 작성해야 할까요?

Spring은 @Valid 어노테이션 하나로 이 모든 것을 해결합니다.
이것이 바로 AOP(관점지향프로그래밍)의 시작입니다!"
```

### 마무리 (5분)
```
"이제 사용자가 잘못된 값을 입력해도 안전하게 처리할 수 있습니다.
하지만 서버에서 예상치 못한 오류가 발생한다면 어떻게 될까요?

다음 시간에는 예외 상황을 어떻게 처리하는지 알아보겠습니다!"
```

---

## 📚 핵심 키워드 정리

- **Bean Validation**: `@NotBlank`, `@Size`, `@Valid`
- **BindingResult**: 검증 오류 결과 객체  
- **횡단 관심사**: 모든 계층에 공통으로 적용되는 기능
- **AOP 기본 개념**: 어노테이션으로 공통 기능 적용
- **DI 활용**: Validator Bean의 의존성 주입
- **사용자 경험**: 친화적 오류 메시지, 입력값 유지

**🎯 핵심 메시지**: "검증은 모든 애플리케이션의 필수 기능이며, Spring의 AOP 개념을 이해하는 첫 단계입니다!"