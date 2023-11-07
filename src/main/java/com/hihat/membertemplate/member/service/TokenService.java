package com.hihat.membertemplate.member.service;

import com.hihat.membertemplate.exception.CustomException;
import com.hihat.membertemplate.member.config.jwt.TokenProvider;
import com.hihat.membertemplate.member.domain.Member;
import com.hihat.membertemplate.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import static com.hihat.membertemplate.member.config.jwt.AuthTokenManager.ACCESS_TOKEN_DURATION;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;
    private final MessageSource messageSource;

    public String createNewAccessToken(String refreshToken) {
        // 리프레시 토큰 유효성 검사
        if (!tokenProvider.validToken(refreshToken)) {
            throw new CustomException(MessageUtil.getMessage(messageSource, "exception.token.invalid"));
        }

        // DB에서 리프레시 토큰 확인 후 유저 검색
        Member member = memberService.findByMemberNo(refreshTokenService.findByRefreshToken(refreshToken).getMemberNo());

        // 토큰 공급자를 통해 새 액세스 토큰 생성
        return tokenProvider.generateToken(member, ACCESS_TOKEN_DURATION);
    }
}
