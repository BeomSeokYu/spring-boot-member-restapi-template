package com.hihat.membertemplate.member.service;

import com.hihat.membertemplate.exception.CustomException;
import com.hihat.membertemplate.member.domain.RefreshToken;
import com.hihat.membertemplate.member.repository.RefreshTokenRepository;
import com.hihat.membertemplate.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MessageSource messageSource;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(MessageUtil.getMessage(messageSource, "exception.token.not-found")));
    }
}
