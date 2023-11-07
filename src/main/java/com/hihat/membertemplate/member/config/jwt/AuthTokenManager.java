package com.hihat.membertemplate.member.config.jwt;

import com.hihat.membertemplate.member.config.jwt.property.TokenInfoProperties;
import com.hihat.membertemplate.member.domain.Member;
import com.hihat.membertemplate.member.domain.RefreshToken;
import com.hihat.membertemplate.member.repository.RefreshTokenRepository;
import com.hihat.membertemplate.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthTokenManager {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public static String REFRESH_TOKEN_COOKIE_NAME;
    public static Duration REFRESH_TOKEN_DURATION;
    public static Duration ACCESS_TOKEN_DURATION;
    public static String REDIRECT_PATH;             // not used

    @Autowired
    public AuthTokenManager(TokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository, TokenInfoProperties tokenInfoProperties) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;

        REFRESH_TOKEN_COOKIE_NAME = tokenInfoProperties.getRefreshTokenCookieName();
        REFRESH_TOKEN_DURATION = Duration.ofDays(tokenInfoProperties.getRefreshTokenExpTime());
        ACCESS_TOKEN_DURATION = Duration.ofHours(tokenInfoProperties.getAccessTokenExpTime());
        REDIRECT_PATH = tokenInfoProperties.getRedirectUri();
    }

    // 리프레시 토큰 생성 및 저장, 쿠키에 저장
    public String progressAuthenticationTokenIssuance(HttpServletRequest request, HttpServletResponse response, Member authenticatedMember) {
        String refreshToken = tokenProvider.generateToken(authenticatedMember, REFRESH_TOKEN_DURATION);
        saveRefreshToken(authenticatedMember.getMemberNo(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        // 액세스 토큰 생성 후 반환
        return tokenProvider.generateToken(authenticatedMember, ACCESS_TOKEN_DURATION);
    }

    // 생성된 리프레시 토큰을 전달받아 데이터베이스에 저장
    private void saveRefreshToken(Long memberNo, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByMemberNo(memberNo)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(memberNo, newRefreshToken));
        refreshTokenRepository.save(refreshToken);
    }

    // 생성된 리프레시 토큰을 쿠키에 저장
    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
//        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
        CookieUtil.addHttpOnlyCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);  // Http Only Cookie에 저장
    }

    // 액세스 토큰을 패스에 추가
    private String getTargetUrl(String token) {
        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}
