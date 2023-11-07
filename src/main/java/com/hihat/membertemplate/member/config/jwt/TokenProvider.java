package com.hihat.membertemplate.member.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.hihat.membertemplate.member.config.jwt.property.JwtProperties;
import com.hihat.membertemplate.member.domain.Member;
import com.hihat.membertemplate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;

    public String generateToken(Member member, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), member);
    }

    // JWT 토큰 생성 메서드
    private String makeToken(Date expiry, Member member) {
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)       // 헤더 typ : JWT
                .setIssuer(jwtProperties.getIssuer())               // 내용 iss : 프로퍼티 설정 issuer 값
                .setIssuedAt(now)                                   // 내용 iat : 현재 시간
                .setExpiration(expiry)                              // 내용 exp : expiry 멤버 변숫값
                .setSubject(member.getMemberId())                       // 내용 sub : 유저의 ID
                .claim("id", member.getMemberNo())                // 클레임 id : 유저 No
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey()) // 서명 : 비밀값과 함께 해시값을 HS256 방식으로 암호화
                .compact();
    }

    // JWT 토큰 유효성 검증 메서드
    public boolean validToken(String token) {
        try{
            Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())    // 비밀값으로 복호화
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) { // 복호화 과정 오류 발생 시 유효하지 않은 토큰
            return false;
        }
    }

    // 토큰 기반으로 인증 정보를 가져오는 메서드
    // 필터에서 토큰의 유효성이 확인되어야 호출
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Member member = memberRepository.findByMemberId(claims.getSubject()).orElse(null);

        if (member == null) { return null; }    // 토큰이 유효하지만 유저가 DB에 존재하지 않는 경우 null일 수 있음

        Collection<? extends GrantedAuthority> authorities = member.getAuthorities();
//        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities), token, authorities);
    }

    // 토큰 기반으로 유저 ID를 가져오는 메서드
    public Long getMemberNo(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .setSigningKey(jwtProperties.getSecretKey())
            .parseClaimsJws(token)
            .getBody();
    }
}
