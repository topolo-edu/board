package io.goorm.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

    /**
     * 메시지 소스 설정
     * - 여러 메시지 번들을 설정
     * - UTF-8 인코딩 설정
     * - 캐시 시간 설정
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        // 메시지 번들 파일들 설정
        messageSource.setBasenames(
            "messages/validation/ValidationMessages",
            "messages/i18n/messages",
            "messages/stock/stock"
        );

        // UTF-8 인코딩 설정
        messageSource.setDefaultEncoding("UTF-8");

        // 개발 시 메시지 변경 즉시 반영 (운영환경에서는 제거 권장)
        messageSource.setCacheSeconds(0);

        // 기본 로케일 설정
        messageSource.setDefaultLocale(Locale.KOREAN);

        // 메시지를 찾지 못할 경우 key를 그대로 반환
        messageSource.setUseCodeAsDefaultMessage(true);

        return messageSource;
    }

    /**
     * 로케일 리졸버 설정
     * - 세션 기반 로케일 저장
     * - 기본 로케일을 한국어로 설정
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.KOREAN);
        return localeResolver;
    }

    /**
     * 로케일 변경 인터셉터 설정
     * - URL 파라미터 'lang'으로 언어 변경 가능
     * - 예: ?lang=en, ?lang=ko
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * 인터셉터 등록
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}