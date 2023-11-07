package com.hihat.membertemplate.member.config.jwt.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "token")
public class TokenInfoProperties {
    private Long refreshTokenExpTime;
    private Long accessTokenExpTime;
    private String refreshTokenCookieName;
    private String redirectUri;
}
