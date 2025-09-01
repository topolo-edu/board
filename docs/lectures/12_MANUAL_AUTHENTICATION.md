# 12강: 수동 인증 시스템 (Manual Authentication) - 보안의 기본을 직접 구현하기!

## 🎯 학습 목표
- **Session 기반 인증** 직접 구현으로 보안 원리 이해
- **HandlerInterceptor**를 통한 AOP 패턴 경험
- **IoC/DI**를 활용한 사용자 관리 시스템 구축

---

## 🚨 현재 문제 상황

### "누구나 다른 사람의 글을 지울 수 있어요!"

**문제점들**:
1. **로그인 없이** 모든 기능 접근 가능
2. **다른 사람이 작성한 게시글**도 수정/삭제 가능
3. **악성 사용자** 차단 불가능
4. **개인화된 서비스** 제공 불가능

```html
<!-- 현재 상태: 누구나 접근 가능 -->
<a href="/posts/new">글쓰기</a>  <!-- 로그인 체크 없음 -->
<button onclick="deletePost()">삭제</button>  <!-- 작성자 확인 없음 -->
```

**위험성**: 악의적인 사용자가 모든 게시글을 삭제할 수 있음! 😱

---

## 💡 해결책: Session 기반 인증 직접 구현

### 1단계: User 엔티티 생성

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Column(unique = true)
    private String email;
    
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 10, message = "이름은 2~10자로 입력해주세요")
    private String name;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String password;
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
    
    // getters/setters...
}

enum UserRole {
    ADMIN, USER
}
```

### 2단계: 인증 서비스 구현 (IoC/DI 활용)

```java
@Service
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // DI로 주입
    
    // 생성자 주입 (IoC/DI)
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // 회원가입
    @Transactional
    public User register(User user) {
        // 이미 존재하는 이메일인지 체크
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("이미 사용중인 이메일입니다.");
        }
        
        // 비밀번호 암호화 (보안!)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    // 로그인 인증
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new DataNotFoundException("존재하지 않는 사용자입니다."));
            
        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("비밀번호가 일치하지 않습니다.");
        }
        
        return user;
    }
}
```

### 3단계: 인증 컨트롤러 구현

```java
@Controller
public class AuthController {
    
    private final UserService userService;
    
    // DI를 통한 의존성 주입
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    // 로그인 폼
    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }
    
    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String email, 
                       @RequestParam String password,
                       HttpSession session,  // Spring이 자동으로 주입
                       RedirectAttributes redirectAttributes) {
        try {
            User user = userService.authenticate(email, password);
            
            // 세션에 사용자 정보 저장 (인증 완료!)
            session.setAttribute("loginUser", user);
            
            return "redirect:/posts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }
    
    // 로그아웃 처리  
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션 무효화
        return "redirect:/";
    }
}
```

### 4단계: 로그인 체크 인터셉터 구현 (AOP 패턴)

```java
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String requestURI = request.getRequestURI();
        
        // 로그인 체크가 필요한 URL인지 확인
        if (isLoginRequired(requestURI)) {
            HttpSession session = request.getSession(false);
            
            if (session == null || session.getAttribute("loginUser") == null) {
                // 로그인되지 않은 사용자 → 로그인 페이지로 리다이렉트
                response.sendRedirect("/login?redirectURL=" + requestURI);
                return false;  // 요청 처리 중단
            }
        }
        
        return true;  // 요청 계속 진행
    }
    
    private boolean isLoginRequired(String requestURI) {
        // 로그인이 필요한 URL 목록
        String[] loginRequiredURLs = {
            "/posts/new", "/posts/*/edit", "/posts/*/delete"
        };
        
        return Arrays.stream(loginRequiredURLs)
                .anyMatch(pattern -> requestURI.matches(pattern.replace("*", "\\d+")));
    }
}
```

### 5단계: 인터셉터 등록 (Spring 설정)

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final LoginCheckInterceptor loginCheckInterceptor;
    
    // DI를 통한 인터셉터 주입
    public WebConfig(LoginCheckInterceptor loginCheckInterceptor) {
        this.loginCheckInterceptor = loginCheckInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .order(1)  // 우선순위 설정
                .addPathPatterns("/**")  // 모든 요청에 적용
                .excludePathPatterns("/", "/login", "/register", "/css/**", "/js/**");
                // 제외할 패턴들
    }
    
    // 비밀번호 암호화를 위한 Bean 등록 (IoC)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 🔗 Spring 핵심 개념 연결

### 🎯 AOP (Aspect Oriented Programming) 패턴 적용

#### "인터셉터는 AOP의 또 다른 형태입니다!"

```java
// 기존 방식: 모든 Controller 메서드에서 로그인 체크
@Controller
public class PostController {
    
    @GetMapping("/posts/new")
    public String newPost(HttpSession session) {
        // 로그인 체크 코드 중복!
        if (session.getAttribute("loginUser") == null) {
            return "redirect:/login";
        }
        return "post/form";
    }
    
    @PostMapping("/posts/{id}/delete") 
    public String delete(@PathVariable Long id, HttpSession session) {
        // 로그인 체크 코드 또 중복!
        if (session.getAttribute("loginUser") == null) {
            return "redirect:/login";
        }
        // 삭제 로직...
    }
}

// AOP 방식: 인터셉터가 모든 요청을 가로채서 처리
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    public boolean preHandle(...) {
        // 한 곳에서 모든 인증 체크!
    }
}
```

**핵심 인사이트**:
- **횡단 관심사**: 인증은 여러 Controller에서 공통 필요
- **관심사 분리**: 비즈니스 로직과 인증 로직 완전 분리
- **선언적 처리**: 설정만으로 URL별 인증 정책 적용
- **Filter vs Interceptor**: Spring MVC 내에서의 AOP 구현

### 🎯 IoC/DI (Inversion of Control / Dependency Injection) 심화

```java
// 복잡한 의존관계들이 모두 DI로 해결
@Service
public class UserService {
    private final UserRepository userRepository;      // DI
    private final PasswordEncoder passwordEncoder;    // DI
}

@Controller  
public class AuthController {
    private final UserService userService;            // DI
}

@Configuration
public class WebConfig {
    private final LoginCheckInterceptor interceptor;  // DI
}

// Spring IoC Container가 모든 의존관계를 자동으로 주입
```

**의존관계 해결 과정**:
1. **Bean 등록**: `@Service`, `@Controller`, `@Component` 스캔
2. **의존관계 파악**: 생성자 매개변수 분석  
3. **순서 결정**: 의존성 그래프 기반 생성 순서 결정
4. **자동 주입**: 런타임에 필요한 객체들 자동 연결

---

## 🛠️ 실습 체크포인트

### ✅ 기본 인증 동작 확인
1. **회원가입** → 비밀번호 암호화되어 저장되는지 확인 (DB 조회)
2. **로그인 성공** → 세션에 사용자 정보 저장되는지 확인
3. **로그인 실패** → 적절한 오류 메시지 표시되는지 확인

### ✅ 접근 제어 확인
1. **미로그인 상태**에서 `/posts/new` 접근 → 로그인 페이지로 리다이렉트
2. **로그인 후** 원래 요청했던 페이지로 자동 이동
3. **로그아웃** → 세션 정보 삭제 및 보호된 페이지 접근 차단

### ✅ AOP 개념 이해 확인  
1. **Controller 메서드에 인증 코드 없음**에도 접근 제어되는 AOP 원리 이해
2. **인터셉터 하나**로 모든 URL 보호되는 횡단 관심사 개념 체감
3. **설정 기반**으로 보안 정책 적용되는 선언적 프로그래밍 이해

---

## 🎪 강의 진행 팁

### 도입부 (8분)
```
"여러분, 지금 만든 게시판에서 다른 사람이 작성한 글을 삭제할 수 있나요?
실제로 해보세요. /posts/1/delete 이런 URL로 직접 접근해보면..."

→ 누구나 삭제 가능한 현실 시연
→ "실제 서비스라면 큰 문제겠죠?" 문제 인식
```

### 보안 원리 설명 (10분)
```
"웹에서 '로그인된 사용자'라는 것을 어떻게 기억할까요?
HTTP는 stateless인데, 어떻게 상태를 유지할까요?

바로 '세션'입니다! 서버가 사용자별로 고유한 세션ID를 발급하고,
브라우저는 쿠키로 이 세션ID를 매번 전송합니다."
```

### AOP 개념 강화 (10분)
```
"로그인 체크를 모든 Controller 메서드마다 작성해야 할까요?
50개 메서드가 있으면 50번 같은 코드를 반복해야 할까요?

인터셉터는 이런 '횡단 관심사'를 한 곳에서 처리하는 AOP 패턴입니다!
지난 시간 @Valid, @ControllerAdvice와 같은 원리죠."
```

### 실무 연결 (7분)
```
"지금 우리가 직접 구현한 인증 시스템, 실무에서도 이렇게 할까요?
- 비밀번호 해싱은? ✅ (BCrypt 사용)
- 세션 관리는? ✅ (Spring Session 활용)  
- CSRF 공격 방어는? ❌ (아직 없음)
- 권한 관리는? ❌ (아직 단순함)

다음 시간에는 이런 부족한 부분들을 해결해주는 
'Spring Security'를 배워보겠습니다!"
```

### 마무리 (5분)
```
"이제 로그인한 사용자만 글을 쓸 수 있게 되었습니다!
하지만 아직 부족한 부분들이 많죠?

실무에서는 이런 보안 기능을 더 강력하게 제공하는 
'Spring Security'라는 프레임워크를 사용합니다.
우리가 직접 구현한 것과 어떻게 다른지 비교해보겠습니다!"
```

---

## 📚 핵심 키워드 정리

- **Session 기반 인증**: 서버가 사용자 상태를 세션으로 관리
- **HandlerInterceptor**: Spring MVC에서 요청을 가로채는 AOP 컴포넌트  
- **횡단 관심사**: 인증은 여러 기능에서 공통으로 필요한 관심사
- **IoC/DI 심화**: 복잡한 의존관계를 Spring Container가 자동 관리
- **보안 기본 원칙**: 비밀번호 암호화, 세션 관리, 접근 제어
- **관심사 분리**: 비즈니스 로직과 보안 로직을 완전히 분리

**🎯 핵심 메시지**: "보안도 AOP로! 직접 구현해보니 인증의 원리를 알겠죠? 다음엔 이것을 더 강력하게 만들어주는 Spring Security를 배워봅시다!"