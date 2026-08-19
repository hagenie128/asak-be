package com.asak.common.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    return http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // 추후 관리자 JWT 인증을 사용할 예정이므로 세션은 사용하지 않음
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // TODO-030: TODO-028 provider와 TODO-029 filter 검증 뒤 /api/admin/login은 permitAll,
        // 그 외 /api/admin/**는 authenticated로 전환한다. kiosk/user 공개 범위는 별도 명시한다.
        // 401(미인증)·403(권한 없음) JSON 응답을 GlobalExceptionHandler/API envelope와 맞추고 CORS preflight는 막지
        // 않는다.
        // JWT 인증 구현 전까지 모든 API를 임시 허용한다.
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOriginPatterns(List.of("*"));

    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    config.setAllowedHeaders(
        List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));

    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    // 모든 API 경로에 CORS 설정 적용
    source.registerCorsConfiguration("/**", config);

    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * TODO-027 연동: 관리자 로그인 시 AuthenticationManager를 사용한다. UserDetailsService/PasswordEncoder와 실제 관리자
   * 계정 공급자가 등록된 뒤 정상·실패 인증을 확인한다.
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
