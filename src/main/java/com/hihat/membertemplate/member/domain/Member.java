package com.hihat.membertemplate.member.domain;

import com.hihat.membertemplate.member.config.security.MemberRole;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "tb_member")
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberNo;

    @Column(unique = true, nullable = false)
    private String memberId;

    @Column(nullable = false)
    private String memberPw;

    private String memberName;

    private String memberTel;

    @Enumerated(EnumType.STRING)            // EnumType 이름 그대로 저장
    private MemberRole memberRole;

    @CreatedDate
    private LocalDateTime memberJoinDate;

    @LastModifiedDate
    private LocalDateTime memberModifyDate;

    @Builder
    public Member(String memberId, String memberPw, String memberName, String memberTel, MemberRole memberRole) {
        this.memberId = memberId;
        this.memberPw = memberPw;
        this.memberName = memberName;
        this.memberTel = memberTel;
        this.memberRole = memberRole;
    }

    public Member update(String memberName, String memberTel, MemberRole memberRole) {
        this.memberName = memberName;
        this.memberTel = memberTel;
        this.memberRole = memberRole;
        return this;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(memberRole.name()));
    }

    @Override
    public String getPassword() {
        return memberPw;
    }

    @Override
    public String getUsername() {
        return memberId;
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 패스워드 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 사용 가능 여부
    @Override
    public boolean isEnabled() {
        return true;
    }
}
