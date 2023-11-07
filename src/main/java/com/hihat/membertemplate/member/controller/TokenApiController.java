package com.hihat.membertemplate.member.controller;

import com.hihat.membertemplate.member.config.jwt.AuthTokenManager;
import com.hihat.membertemplate.member.dto.CreateAccessTokenResponse;
import com.hihat.membertemplate.member.service.TokenService;
import com.hihat.membertemplate.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class TokenApiController {

    private final TokenService tokenService;

    @PostMapping("/token")
    public ResponseEntity<CreateAccessTokenResponse> createAccessToken(HttpServletRequest request) {
        Cookie refreshTokenCookie = CookieUtil.getCookie(request, AuthTokenManager.REFRESH_TOKEN_COOKIE_NAME)
                .orElse(null);

        if (refreshTokenCookie == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String newAccessToken = tokenService.createNewAccessToken(refreshTokenCookie.getValue());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }

}
