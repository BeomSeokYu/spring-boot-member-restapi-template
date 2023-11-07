package com.hihat.membertemplate.member.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_refresh")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long refreshNo;

    @Column(nullable = false, unique = true)
    private Long memberNo;

    @Column(nullable = false)
    private String refreshToken;

    @LastModifiedDate
    private LocalDateTime tokenCreateDate;

    public RefreshToken(Long memberNo, String refreshToken) {
        this.memberNo = memberNo;
        this.refreshToken = refreshToken;
    }

    public RefreshToken update(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
        return this;
    }
}
