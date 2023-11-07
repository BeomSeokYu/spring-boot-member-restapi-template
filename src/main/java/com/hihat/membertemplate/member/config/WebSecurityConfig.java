package com.hihat.membertemplate.member.config;

import com.hihat.membertemplate.member.config.jwt.TokenAuthenticationFilter;
import com.hihat.membertemplate.member.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.stream.Stream;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final TokenProvider tokenProvider;

    private static final String[] PERMIT_ALL_PATTERNS = new String[] {
            "/token",
            "/members/login",
            "/members/signup",
            "/members/logout"
    };

    private static final String[] AUTHENTICATED_PATTERNS = new String[] {
            "/members/**",
            "/"
    };

    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring()
                .requestMatchers(toH2Console())
                .requestMatchers(new AntPathRequestMatcher("/static/**"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf().disable()           // CSRF protection 사용 안함
            .httpBasic().disable()      // Http basic Auth 기반 로그인 인증창 사용 안함
            .formLogin().disable()      // Form login 사용 안함
            .logout().disable();        // Logout 사용 안함

        // 세션 정책 - STATELESS: 스프링 시큐리티가 세션을 생성하지도 않고 존재해도 사용하지 않음
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 알려진 UsernamePasswordAuthenticationFilter 필터 앞에 TokenAuthenticationFilter 적용
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // 리소스(URL)의 권한 설정
        http.authorizeRequests(auth ->
                auth.requestMatchers(
                            Stream
                                    .of(PERMIT_ALL_PATTERNS)
                                    .map(AntPathRequestMatcher::new)
                                    .toArray(AntPathRequestMatcher[]::new)
                        ).permitAll()
                    .requestMatchers(
                            Stream
                                    .of(AUTHENTICATED_PATTERNS)
                                    .map(AntPathRequestMatcher::new)
                                    .toArray(AntPathRequestMatcher[]::new)
                    ).authenticated()
                    .anyRequest().permitAll()
        );

        // 인증/인가 시 예외 처리
        http.exceptionHandling()
                .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**"));

        return http.build();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}