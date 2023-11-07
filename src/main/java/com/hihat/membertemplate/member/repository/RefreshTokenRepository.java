package com.hihat.membertemplate.member.repository;

import com.hihat.membertemplate.member.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMemberNo(Long memberNo);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    void deleteByMemberNo(Long memberNo);
}
