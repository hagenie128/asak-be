package com.asak.common.security;

// TODO-061: createToken / validateToken / getClaims를 구현해 TODO-060 로그인 Controller에서 사용한다.
// secret·issuer·만료 시간은 소스에 하드코딩하지 않고 설정으로 주입하며, username/role 등 필요한 최소 claim만 담는다.
// 만료·서명 오류·형식 오류를 구분해 filter가 401로 변환할 수 있게 하고, 정상/만료/변조 token 테스트를 추가한다.
public class JwtTokenProvider {}
