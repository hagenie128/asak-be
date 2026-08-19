package com.asak.common.security;

// TODO-029: OncePerRequestFilter를 구현해 Authorization: Bearer <token>을 파싱하고 TODO-028 provider로 검증한다.
// /api/admin/login과 OPTIONS는 건너뛰며, 그 외 /api/admin/**만 SecurityContext에 인증을 넣는다.
// 키오스크(/api/user/**)는 JWT 미적용이다. 누락·만료·위조 token은 500이 아닌 인증 실패 계약으로 처리하고 TODO-030과 함께 검증한다.
public class JwtAuthenticationFilter {}
