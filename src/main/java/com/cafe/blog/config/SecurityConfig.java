package com.cafe.blog.config;

import com.cafe.blog.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/posts/**").authenticated()  // 인증된 사용자만 접근 가능
                                .anyRequest().permitAll()  // 그 외 요청은 모두 허용
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")  // 로그인 페이지 경로 설정
                                .permitAll()  // 로그인 페이지 접근 허용
                )
                .logout(logout ->
                        logout
                                .permitAll()  // 로그아웃 페이지 접근 허용
                )
                .csrf(csrf -> csrf.disable());  // CSRF 보호 비활성화 (API 서버의 경우 필요에 따라 설정)

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 비밀번호 암호화를 위한 인코더
    }
}
