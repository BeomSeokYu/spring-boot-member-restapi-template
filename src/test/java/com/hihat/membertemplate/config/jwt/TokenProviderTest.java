package com.hihat.membertemplate.config.jwt;

import io.jsonwebtoken.Jwts;
import com.hihat.membertemplate.member.config.jwt.TokenProvider;
import com.hihat.membertemplate.member.config.jwt.property.JwtProperties;
import com.hihat.membertemplate.member.config.security.MemberRole;
import com.hihat.membertemplate.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    public Member createMember() {
        return new Member(
                1L,
                "testuser",
                "qwer1234!",
                null,
                null,
                MemberRole.ROLE_USER,
                null,
                null
        );
    }

    @Test
    @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰 제작")
    void generateToken() {
        // given
        Member testMember = createMember();

        // when
        String token = tokenProvider.generateToken(testMember, Duration.ofMinutes(60));

        // then
        String memberId = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        assertThat(memberId).isEqualTo(testMember.getMemberId()); // 생성된 토큰에서 파싱한 memberId와 생성 유저 정보의 memberId 일치 확인

    }

    @Test
    @DisplayName("validToken(): 토큰 유효성 검사")
    void validToken() {
        // given
        Member testMember = createMember();
        String token = tokenProvider.generateToken(testMember, Duration.ofMinutes(60));

        // when
        Boolean result = tokenProvider.validToken(token);

        // then
        assertThat(result).isTrue();    // 토큰이 유효하다면 True
    }

    @Test
    @DisplayName("getAuthentication(): 토큰 정보를 기반으로 DB에 저장된 사용자의 인증 정보를 가져옴")
    @Transactional
    void getAuthentication() {
        // given
        Member testMember = createMember();
//        memberRepository.save(testMember);    // data.sql로 처리
        String token = tokenProvider.generateToken(testMember, Duration.ofMinutes(60));

        // when
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(testMember.getMemberId());    // memberId 일치 확인
        assertThat(authentication.getAuthorities()                                                                      // 권한 정보 일치 확인
                .stream().map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(MemberRole.ROLE_ADMIN.name()))).isTrue();

    }

    @Test
    @DisplayName("getMemberId(): 토큰 정보를 기반으로 DB에 저장된 사용자의 인증 정보를 가져옴")
    void getMemberId() {
        // given
        Member testMember = createMember();
        String token = tokenProvider.generateToken(testMember, Duration.ofMinutes(60));

        // when
        Long memberNo = tokenProvider.getMemberNo(token);

        // then
        assertThat(memberNo).isEqualTo(testMember.getMemberNo()); // 토큰에 기록한 memberNo 일치 확인
    }
}