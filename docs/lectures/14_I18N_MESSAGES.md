# 14강: 국제화 및 메시지 외부화 (i18n & Messages) - 글로벌 서비스 준비!

## 🎯 학습 목표
- **MessageSource**를 통한 메시지 외부화 및 DI 이해
- **다국어 지원 시스템** 구축으로 국제화(i18n) 학습
- **LocaleChangeInterceptor**를 통한 AOP 패턴 완성

---

## 🚨 현재 문제 상황

### "외국인 사용자가 사이트를 사용하려면?"

**하드코딩된 메시지들**:
```html
<!-- 현재 상태: 한글 하드코딩 -->
<button type="submit">저장</button>
<button type="button">취소</button>
<h1>게시판</h1>
<span>작성자:</span>
<span>작성일:</span>

<!-- 검증 메시지도 하드코딩 -->
@NotBlank(message = "제목은 필수입니다")
@Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
```

**문제점들**:
1. **다국어 서비스 불가능** → 글로벌 진출 제약
2. **메시지 일관성 관리 어려움** → "저장" vs "등록" vs "확인" 혼재
3. **변경 시 모든 파일 수정** → 유지보수 지옥
4. **기획자/디자이너와 협업 어려움** → 코드를 몰라도 텍스트 수정 불가

---

## 💡 해결책: MessageSource + 국제화(i18n)

### 1단계: MessageSource 설정 (IoC/DI)

```java
@Configuration
public class MessageConfig {
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = 
            new ReloadableResourceBundleMessageSource();
            
        messageSource.setBasename("classpath:/messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(60); // 개발 시 실시간 반영
        messageSource.setFallbackToSystemLocale(false); // 기본 언어 고정
        
        return messageSource;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN); // 기본 언어: 한국어
        return resolver;
    }
    
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // URL 파라미터: ?lang=en
        return interceptor;
    }
}

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final LocaleChangeInterceptor localeChangeInterceptor;
    
    // DI를 통한 인터셉터 주입
    public WebConfig(LocaleChangeInterceptor localeChangeInterceptor) {
        this.localeChangeInterceptor = localeChangeInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor); // AOP 적용
    }
}
```

### 2단계: 메시지 파일 생성

```properties
# src/main/resources/messages/messages_ko.properties (한국어)
# 공통 메시지
common.save=저장
common.cancel=취소
common.edit=수정
common.delete=삭제
common.search=검색
common.list=목록
common.detail=상세

# 게시판 메시지
post.list.title=게시판
post.list.new=글쓰기
post.list.no-data=등록된 게시글이 없습니다.
post.form.title.label=제목
post.form.author.label=작성자
post.form.content.label=내용
post.detail.author=작성자:
post.detail.date=작성일:
post.detail.views=조회수:

# 검증 메시지
validation.required=필수 입력 항목입니다.
validation.title.required=제목은 필수입니다.
validation.title.size=제목은 1~200자로 입력해주세요.
validation.content.required=내용은 필수입니다.
validation.content.size=내용은 10~4000자로 입력해주세요.

# 로그인 메시지
login.title=로그인
login.email=이메일
login.password=비밀번호
login.submit=로그인
login.failed=이메일 또는 비밀번호가 올바르지 않습니다.
```

```properties
# src/main/resources/messages/messages_en.properties (영어)
# 공통 메시지
common.save=Save
common.cancel=Cancel
common.edit=Edit
common.delete=Delete
common.search=Search
common.list=List
common.detail=Detail

# 게시판 메시지
post.list.title=Board
post.list.new=New Post
post.list.no-data=No posts found.
post.form.title.label=Title
post.form.author.label=Author
post.form.content.label=Content
post.detail.author=Author:
post.detail.date=Date:
post.detail.views=Views:

# 검증 메시지
validation.required=This field is required.
validation.title.required=Title is required.
validation.title.size=Title must be 1-200 characters.
validation.content.required=Content is required.
validation.content.size=Content must be 10-4000 characters.

# 로그인 메시지
login.title=Login
login.email=Email
login.password=Password
login.submit=Sign In
login.failed=Invalid email or password.
```

### 3단계: Entity 검증 메시지 외부화

```java
@Entity
public class Post {
    
    @NotBlank(message = "{validation.title.required}")  // 메시지 키 참조
    @Size(min = 1, max = 200, message = "{validation.title.size}")
    private String title;
    
    @NotBlank(message = "{validation.content.required}")
    @Size(min = 10, max = 4000, message = "{validation.content.size}")
    private String content;
    
    // ...
}
```

### 4단계: 타임리프에서 메시지 사용

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layouts/default}">
<head>
    <!-- 동적 제목 -->
    <title th:text="#{post.list.title}">게시판</title>
</head>

<body>
<div layout:fragment="content">
    <section class="max-w-7xl mx-auto px-4 py-12">
        <!-- 언어 선택 -->
        <div class="mb-4 text-right">
            <a th:href="@{''(lang=ko)}" 
               th:class="${#locale.language == 'ko'} ? 'font-bold' : ''">한국어</a> |
            <a th:href="@{''(lang=en)}"
               th:class="${#locale.language == 'en'} ? 'font-bold' : ''">English</a>
        </div>
        
        <!-- 페이지 헤더 -->
        <div class="flex justify-between items-center mb-6">
            <h2 th:text="#{post.list.title}" class="text-3xl font-bold">게시판</h2>
            <a th:href="@{/posts/new}" 
               th:text="#{post.list.new}"
               class="px-4 py-2 bg-blue-600 text-white rounded">글쓰기</a>
        </div>

        <!-- 테이블 헤더 -->
        <table class="min-w-full">
            <thead>
                <tr>
                    <th th:text="#{post.form.title.label}">제목</th>
                    <th th:text="#{post.form.author.label}">작성자</th>
                    <th th:text="#{post.detail.date}">작성일</th>
                    <th th:text="#{post.detail.views}">조회수</th>
                </tr>
            </thead>
            <tbody>
                <!-- 데이터 없을 때 -->
                <tr th:if="${posts.empty}">
                    <td colspan="4" th:text="#{post.list.no-data}">등록된 게시글이 없습니다.</td>
                </tr>
                <!-- 게시글 목록 -->
                <tr th:each="post : ${posts}">
                    <td th:text="${post.title}">제목</td>
                    <td th:text="${post.author}">작성자</td>
                    <td th:text="${#temporals.format(post.createdAt, 'yyyy-MM-dd')}">날짜</td>
                    <td th:text="${post.viewCount}">조회수</td>
                </tr>
            </tbody>
        </table>
    </section>
</div>
</body>
</html>
```

### 5단계: 폼에서 메시지 사용

```html
<!-- 게시글 작성 폼 -->
<form th:action="@{/posts}" th:object="${post}" method="post">
    <div>
        <label th:text="#{post.form.title.label} + ' *'">제목 *</label>
        <input type="text" th:field="*{title}" th:placeholder="#{post.form.title.label}">
        
        <!-- 검증 오류 (자동으로 다국어 메시지 표시) -->
        <div th:if="${#fields.hasErrors('title')}" class="error-message">
            <span th:each="error : ${#fields.errors('title')}" th:text="${error}"></span>
        </div>
    </div>
    
    <div>
        <label th:text="#{post.form.content.label} + ' *'">내용 *</label>
        <textarea th:field="*{content}" th:placeholder="#{post.form.content.label}"></textarea>
        
        <div th:if="${#fields.hasErrors('content')}" class="error-message">
            <span th:each="error : ${#fields.errors('content')}" th:text="${error}"></span>
        </div>
    </div>
    
    <div class="flex justify-between">
        <a th:href="@{/posts}" th:text="#{common.cancel}">취소</a>
        <button type="submit" th:text="#{common.save}">저장</button>
    </div>
</form>
```

---

## 🔗 Spring 핵심 개념 연결

### 🎯 IoC/DI (Inversion of Control / Dependency Injection) 활용

#### "MessageSource는 DI의 완벽한 활용 사례입니다!"

```java
@Service
public class PostService {
    
    private final MessageSource messageSource;  // DI로 주입
    
    // 생성자 주입
    public PostService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    public void validatePost(Post post) {
        if (post.getTitle().isEmpty()) {
            // 현재 로케일에 맞는 메시지 동적 생성
            String message = messageSource.getMessage(
                "validation.title.required", 
                null, 
                LocaleContextHolder.getLocale()
            );
            throw new ValidationException(message);
        }
    }
}

// Spring이 자동으로 주입해주는 것들:
// - MessageSource Bean
// - LocaleResolver Bean  
// - LocaleChangeInterceptor Bean
```

### 🎯 전략 패턴 (Strategy Pattern) 구현

```java
// Locale에 따른 다른 메시지 전략
Locale korean = new Locale("ko");
String koreanMessage = messageSource.getMessage("common.save", null, korean);
// → "저장"

Locale english = new Locale("en"); 
String englishMessage = messageSource.getMessage("common.save", null, english);
// → "Save"

// 동일한 키, 다른 전략으로 다른 결과!
```

### 🎯 AOP (Aspect Oriented Programming) 완성

#### "LocaleChangeInterceptor도 횡단 관심사입니다!"

```java
@Component
public class LocaleChangeInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        
        String language = request.getParameter("lang");
        if (language != null) {
            // 모든 요청에서 언어 변경 체크 (횡단 관심사!)
            LocaleContextHolder.setLocale(new Locale(language));
        }
        
        return true;
    }
}
```

**AOP 체계 완성**:
- **검증**: `@Valid` (10강)
- **예외 처리**: `@ControllerAdvice` (11강)  
- **보안**: `HandlerInterceptor` (12강), `Filter Chain` (13강)
- **국제화**: `LocaleChangeInterceptor` (14강)

---

## 🛠️ 실습 체크포인트

### ✅ 메시지 외부화 확인
1. **하드코딩 제거** → "저장", "취소" 등이 모두 properties에서 로드
2. **일관성 확보** → 동일한 의미는 동일한 키로 통일
3. **유지보수 개선** → properties 파일만 수정해도 모든 페이지 적용

### ✅ 다국어 지원 확인
1. **언어 전환** → URL에 `?lang=en` 추가 시 영어로 전환
2. **세션 유지** → 언어 선택 후 다른 페이지 이동해도 언어 유지
3. **검증 메시지** → 폼 에러도 선택된 언어로 표시

### ✅ Spring 핵심 개념 이해
1. **DI 활용** → MessageSource가 어떻게 주입되어 사용되는지 이해
2. **전략 패턴** → 동일 키로 다른 언어 메시지 제공하는 원리
3. **AOP 완성** → 모든 횡단 관심사를 Interceptor로 처리하는 구조 체감

---

## 🎪 강의 진행 팁

### 도입부 (5분)
```
"여러분의 게시판이 해외에서도 서비스된다면?
미국, 일본, 독일 사용자들이 사용한다면?

현재는 '저장', '취소' 같은 버튼들이 모두 한글로 하드코딩되어 있죠.
이것을 어떻게 해결할까요?"

→ 글로벌 서비스의 필요성 인식
```

### 메시지 외부화 효과 (10분)
```
"일단 다국어를 떠나서, 메시지를 외부로 빼는 것만으로도 큰 장점이 있어요:

1. 일관성: '저장' vs '등록' vs '확인' → 통일
2. 유지보수: 한 곳만 바꿔도 모든 페이지 적용
3. 협업: 기획자가 코드 건드리지 않고도 텍스트 수정
4. 품질: 오타, 맞춤법 검사를 한 곳에서!"
```

### DI와 전략 패턴 (10분)
```
"MessageSource는 DI의 완벽한 예시입니다:

같은 키 'common.save'인데:
- 한국어 로케일 → '저장'  
- 영어 로케일 → 'Save'
- 일본어 로케일 → '保存'

하나의 인터페이스(MessageSource)로 다양한 전략을 제공하죠.
이것이 바로 전략 패턴입니다!"
```

### AOP 체계 완성 (10분)
```
"지금까지 배운 AOP들을 정리해보면:

10강: @Valid → 검증이라는 횡단 관심사
11강: @ControllerAdvice → 예외 처리라는 횡단 관심사  
12강: HandlerInterceptor → 인증이라는 횡단 관심사
13강: Filter Chain → 보안이라는 횡단 관심사
14강: LocaleChangeInterceptor → 국제화라는 횡단 관심사

모든 비즈니스 로직과 분리된 '횡단 관심사'들이죠!
이것이 바로 Spring의 핵심 철학 AOP입니다!"
```

### 마무리 (5분)
```
"이제 여러분의 게시판은:
✅ 데이터 검증으로 안전하고
✅ 예외 처리로 안정적이고  
✅ 인증으로 보안이 되고
✅ 다국어로 글로벌 준비까지!

진짜 실무에서 사용할 수 있는 수준이 되었습니다! 🎉"
```

---

## 🌍 실무 확장 가능성

### 추가 언어 지원
```properties
# messages_ja.properties (일본어)
common.save=保存
common.cancel=キャンセル

# messages_zh.properties (중국어)  
common.save=保存
common.cancel=取消
```

### 메시지 파라미터 활용
```properties
# 동적 메시지
welcome.message=안녕하세요, {0}님! {1}개의 새 글이 있습니다.
```

```java
// Java 코드에서 사용
String message = messageSource.getMessage(
    "welcome.message", 
    new Object[]{"홍길동", 5}, 
    locale
);
// → "안녕하세요, 홍길동님! 5개의 새 글이 있습니다."
```

---

## 📚 핵심 키워드 정리

- **MessageSource**: Spring의 메시지 외부화 및 국제화 핵심 인터페이스
- **i18n (Internationalization)**: 국제화, 다국어 지원 시스템
- **LocaleResolver**: 사용자의 언어/지역 설정을 결정하는 전략 컴포넌트
- **LocaleChangeInterceptor**: 언어 변경 요청을 처리하는 AOP 컴포넌트
- **전략 패턴**: 동일 키로 다른 언어 메시지 제공하는 설계 패턴
- **관심사 분리**: 비즈니스 로직과 표현 계층 완전 분리

**🎯 핵심 메시지**: "메시지 외부화와 국제화는 단순한 기능이 아닙니다. Spring의 DI, AOP, 전략 패턴이 모두 집약된 완성형 아키텍처입니다!"