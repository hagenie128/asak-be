// 해당 페이지는 되도록이면 마지막 부분 -> 기능 구현 다 하고 추후 작업
// but url로 어느 부분까지는 접근을 해줄지 정도는 적어두는게 좋음
// 고객 키오스크는 jwt(키오스크 단말 자체 토큰)로 인증 X , 관리자만 O 적용

package com.asak.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
      throws Exception {

    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll())
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .build();
  }
}