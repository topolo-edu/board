# 13강: Spring Security 도입 (Spring Security) - 프레임워크 vs 수동 구현 비교!

## 🎯 학습 목표
- **Spring Security**와 수동 구현 비교를 통한 프레임워크 가치 체감
- **Filter Chain** 기반 AOP의 극대화 이해
- **설정 기반 보안 정책** 적용 방법 학습

---

## 🚨 수동 구현의 한계점

### "우리가 만든 인증 시스템, 정말 안전할까요?"

**부족한 부분들**:
1. **CSRF 공격** 방어 기능 없음
2. **세션 고정 공격** 대응 부족  
3. **브루트 포스 공격** 차단 없음
4. **권한 관리** 시스템 복잡함
5. **보안 헤더** 설정 누락

```java
// 현재 우리 코드: 기본적인 인증만 가능
@PostMapping("/login")
public String login(String email, String password, HttpSession session) {
    User user = userService.authenticate(email, password);
    session.setAttribute("loginUser", user);  // 세션 고정 공격 위험!
    return "redirect:/posts";
}
```

**실무에서 추가로 필요한 것들**:
- CSRF 토큰 검증
- 세션 ID 재생성  
- 로그인 실패 횟수 제한
- 보안 헤더 자동 설정
- 권한별 세밀한 접근 제어

---

## 💡 해결책: Spring Security 도입

### 1단계: 의존성 추가

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    // 추가된 한 줄로 엔터프라이즈급 보안 기능 모두 활성화!
}
```

### 2단계: Security 설정 클래스 생성

```java
@Configuration
@EnableWebSecurity  // Spring Security 활성화
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    
    // DI를 통한 사용자 서비스 주입
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 📋 URL별 접근 권한 설정 (선언적 보안!)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()
                .requestMatchers("/posts/new", "/posts/*/edit", "/posts/*/delete").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            
            // 🔐 로그인 설정
            .formLogin(form -> form
                .loginPage("/login")                    // 커스텀 로그인 페이지
                .loginProcessingUrl("/login")           // 로그인 처리 URL
                .defaultSuccessUrl("/posts", true)     // 로그인 성공 시 이동
                .failureUrl("/login?error=true")       // 로그인 실패 시 이동
                .usernameParameter("email")             // 사용자명 파라미터 변경
                .permitAll()
            )
            
            // 🚪 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)           // 세션 무효화
                .deleteCookies("JSESSIONID")           // 쿠키 삭제
                .permitAll()
            )
            
            // 🛡️ CSRF 보호 (자동 활성화!)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCSrfTokenRepository.withHttpOnlyFalse())
            )
            
            // 🔒 세션 관리 (세션 고정 공격 방어!)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)                     // 동시 세션 1개 제한
                .maxSessionsPreventsLogin(false)        // 기존 세션 만료 처리
            );
            
        return http.build();
    }
    
    // 🔐 비밀번호 인코더 (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 3단계: UserDetailsService 구현 (Spring Security와 연동)

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
            
        // User 엔티티를 Spring Security UserDetails로 변환
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPassword())  // 이미 암호화된 비밀번호
            .roles(user.getRole().name())  // ROLE_USER, ROLE_ADMIN
            .build();
    }
}
```

### 4단계: 타임리프에서 CSRF 토큰 적용

```html
<!-- 로그인 폼 -->
<form th:action="@{/login}" method="post">
    <!-- Spring Security가 자동으로 CSRF 토큰 주입 -->
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    
    <input type="email" name="email" placeholder="이메일" required>
    <input type="password" name="password" placeholder="비밀번호" required>
    <button type="submit">로그인</button>
</form>

<!-- 게시글 삭제 폼 -->
<form th:action="@{/posts/{id}/delete(id=${post.id})}" method="post">
    <!-- CSRF 토큰이 자동으로 추가됨 (th:action 사용 시) -->
    <button type="submit" onclick="return confirm('정말 삭제하시겠습니까?')">삭제</button>
</form>
```

### 5단계: 기존 수동 구현 코드 제거

```java
// ❌ 제거할 코드들
// - LoginCheckInterceptor 클래스 전체
// - WebConfig의 인터셉터 등록 부분  
// - AuthController의 로그인 처리 로직
// - 수동 세션 관리 코드

// ✅ Spring Security가 모든 것을 자동 처리!
```

---

## 🔗 Spring 핵심 개념 연결

### 🎯 AOP (Aspect Oriented Programming) 극대화

#### "Spring Security는 AOP의 완벽한 구현체입니다!"

```java
// 수동 구현: 개발자가 직접 AOP 구현
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    public boolean preHandle(...) { ... }  // 수동 구현
}

// Spring Security: 프레임워크가 제공하는 완성된 AOP
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // 설정만으로 모든 AOP 처리 완료!
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/posts/new").authenticated()  // 선언적 보안
    )
}
```

**Filter Chain의 AOP 구조**:
```
Client Request
    ↓
SecurityContextPersistenceFilter  (세션 관리)
    ↓  
CsrfFilter                       (CSRF 방어)
    ↓
UsernamePasswordAuthenticationFilter  (인증 처리)
    ↓
AuthorizationFilter              (권한 검사)
    ↓
Your Controller                  (비즈니스 로직)
```

### 🎯 IoC/DI (Inversion of Control / Dependency Injection) 고급 활용

```java
// 복잡한 Security 컴포넌트들이 모두 DI로 연결
@Configuration
public class SecurityConfig {
    private final UserDetailsService userDetailsService;     // DI
    private final PasswordEncoder passwordEncoder;           // DI
    private final AuthenticationSuccessHandler successHandler; // DI (선택)
    private final AccessDeniedHandler accessDeniedHandler;   // DI (선택)
}

// Spring Security가 내부적으로 수십 개의 Bean을 자동 등록하고 연결
// AuthenticationManager, AuthenticationProvider, SecurityFilterChain 등등...
```

### 🎯 설정 기반 프로그래밍 (Configuration over Code)

```java
// 수동 구현: 코드로 로직 구현
public boolean preHandle(...) {
    if (requestURI.startsWith("/posts/new")) {
        if (session.getAttribute("loginUser") == null) {
            response.sendRedirect("/login");
            return false;
        }
    }
    return true;
}

// Spring Security: 설정으로 정책 선언
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/posts/new").authenticated()
    .requestMatchers("/admin/**").hasRole("ADMIN")
)
```

---

## 🛠️ 실습 체크포인트

### ✅ Spring Security 기본 동작 확인
1. **자동 로그인 페이지** 생성 확인 (`/login` 접근 시)
2. **CSRF 토큰** 자동 생성 및 검증 확인
3. **세션 관리** 개선 확인 (세션 ID 재생성)

### ✅ 수동 구현과 비교
1. **코드량 감소** → 인터셉터 삭제, 설정만으로 보안 적용
2. **기능 향상** → CSRF 방어, 세션 보안, 보안 헤더 자동 적용  
3. **유지보수성** → 설정 변경만으로 보안 정책 수정 가능

### ✅ AOP 개념 심화 이해
1. **Filter Chain**이 모든 요청을 가로채는 AOP 구조 이해
2. **선언적 보안**이 어떻게 구현되는지 체감
3. **관심사의 완전한 분리** → 비즈니스 로직에서 보안 코드 완전 제거

---

## 📊 수동 구현 vs Spring Security 비교

| 구분 | 수동 구현 | Spring Security |
|------|----------|-----------------|
| **코드량** | 많음 (200+ 줄) | 적음 (50줄) |
| **보안 기능** | 기본적 (인증만) | 완전함 (인증+권한+방어) |
| **CSRF 방어** | ❌ 없음 | ✅ 자동 |
| **세션 보안** | ❌ 기본 수준 | ✅ 엔터프라이즈 수준 |
| **권한 관리** | ❌ 복잡함 | ✅ 선언적 설정 |
| **유지보수** | ❌ 어려움 | ✅ 설정 변경만 |
| **확장성** | ❌ 제한적 | ✅ 무제한 |
| **테스트** | ❌ 복잡함 | ✅ 테스트 지원 |

---

## 🎪 강의 진행 팁

### 도입부 (5분)
```
"지난 시간에 우리가 직접 만든 인증 시스템, 잘 작동하죠?
하지만 실무에서는 이것만으로 충분할까요?

CSRF 공격이 뭔지 아시나요? 세션 고정 공격은?
악의적인 해커가 여러분의 사이트를 공격한다면..."

→ 보안 위험성 인식시키기
```

### Spring Security 도입 효과 (15분)
```
"이제 Spring Security를 추가해보겠습니다.
build.gradle에 의존성 하나만 추가하면...

어? 갑자기 모든 페이지에 로그인이 필요해졌네요!
이게 바로 Spring Security의 기본 정책입니다.

설정 파일 하나로 이 모든 것을 제어할 수 있습니다!"
```

### AOP 개념 강화 (10분)
```
"여러분, 우리가 만든 인터셉터와 Spring Security의 차이점이 보이나요?

인터셉터: 1개의 체크포인트
Spring Security: 10여 개의 Filter들이 연쇄적으로 처리

각각의 Filter가 하나의 Aspect(관점)를 담당합니다:
- 인증 Filter, 권한 Filter, CSRF Filter, 세션 Filter...
이것이 바로 AOP의 극대화입니다!"
```

### 실무 가치 설명 (8분)
```
"실무에서는 왜 Spring Security를 쓸까요?

1. 검증된 보안 → 수많은 회사에서 사용, 보안 전문가들이 개발
2. 표준화 → 다른 개발자도 쉽게 이해 가능  
3. 확장성 → OAuth, JWT, LDAP 등 모든 인증 방식 지원
4. 유지보수 → 설정 변경만으로 보안 정책 수정

우리가 직접 이 모든 것을 구현하려면 몇 개월이 걸릴 것들을
설정 몇 줄로 해결할 수 있습니다!"
```

### 마무리 (2분)
```
"이제 엔터프라이즈급 보안이 적용되었습니다!
하지만 아직 하나 더 개선할 것이 있어요.

'저장', '취소' 같은 메시지들이 코드에 하드코딩되어 있죠?
다음 시간에는 이것을 어떻게 관리하는지 알아보겠습니다!"
```

---

## 📚 핵심 키워드 정리

- **Spring Security**: 엔터프라이즈급 보안 프레임워크
- **Filter Chain**: 연쇄적 AOP 처리를 통한 완전한 보안
- **선언적 보안**: 코드가 아닌 설정으로 보안 정책 관리
- **CSRF 방어**: 자동 토큰 생성 및 검증으로 공격 방어  
- **설정 기반**: Configuration over Code 원칙
- **프레임워크 가치**: 수동 구현 대비 압도적 기능과 안정성

**🎯 핵심 메시지**: "직접 만들어보니 프레임워크의 가치를 알겠죠? Spring Security는 AOP의 완성체이며, 실무에서 반드시 사용해야 하는 필수 도구입니다!"