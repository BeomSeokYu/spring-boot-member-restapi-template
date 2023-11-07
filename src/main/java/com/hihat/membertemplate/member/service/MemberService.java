package com.hihat.membertemplate.member.service;

import com.hihat.membertemplate.exception.CustomException;
import com.hihat.membertemplate.member.config.security.MemberRole;
import com.hihat.membertemplate.member.domain.Member;
import com.hihat.membertemplate.member.repository.MemberRepository;
import com.hihat.membertemplate.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MessageSource messageSource;
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    public Member login(String memberId, String memberPw) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(MessageUtil.getMessage(messageSource, "exception.member.invalid")));
        if (!getBCryptPasswordEncoder().matches(memberPw, member.getPassword())) {
            throw new CustomException(MessageUtil.getMessage(messageSource, "exception.member.password-mismatch"));
        }
        return member;
    }

    public Long signup(Member member) {
        if (memberRepository.findByMemberId(member.getMemberId()).isPresent()) {
            throw new CustomException(MessageUtil.getMessage(messageSource, "exception.member.id-already-exists"));
        }
        try {
            return memberRepository.save(Member.builder()
                    .memberId(member.getMemberId())
                    .memberPw(getBCryptPasswordEncoder().encode(member.getMemberPw()))
                    .memberName(member.getMemberName())
                    .memberTel(member.getMemberTel())
                    .memberRole(getMemberRole(member.getMemberRole()))
                    .build()).getMemberNo();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(MessageUtil.getMessage(messageSource, "exception.member.registration-fail"));
        }

    }

    private static MemberRole getMemberRole(MemberRole role) {
        if (role != null) {
            return role;
        }
        return MemberRole.ROLE_USER;
    }

    public Member findByMemberNo(long memberNo) {
        return memberRepository.findByMemberNo(memberNo)
                .orElseThrow(() -> new CustomException(MessageUtil.getMessage(messageSource, "exception.member.not-found")));
    }

    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(MessageUtil.getMessage(messageSource, "exception.member.not-found")));
    }

    @Transactional
    public Member editMember(long memberNo, Member requestMember) {
        Member member = memberRepository.findByMemberNo(memberNo)
                .orElseThrow(() -> new CustomException(MessageUtil.getMessage(messageSource, "exception.member.not-found")));

        member.update(
                requestMember.getMemberName(),
                requestMember.getMemberTel(),
                requestMember.getMemberRole()
        );
        return member;
    }

    public void removeMember(long memberNo) {
        memberRepository.deleteByMemberNo(memberNo);
    }

    private BCryptPasswordEncoder getBCryptPasswordEncoder() {
        if (bCryptPasswordEncoder == null) {
            bCryptPasswordEncoder = new BCryptPasswordEncoder();
        }
        return bCryptPasswordEncoder;
    }

    public boolean isAdminOrSelf(long memberNo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 관리자 Role 여부 확인
        if (hasAdminRole(authentication)) return true;

        // 본인 여부 확인
        if (isSelf(memberNo, authentication)) return true;

        return false;
    }

    private boolean isSelf(long memberNo, Authentication authentication) {
        Member member = memberRepository.findByMemberNo(memberNo)
                .orElseThrow(() -> new CustomException(MessageUtil.getMessage(messageSource, "exception.member.not-found")));
        return member.getMemberId().equals(authentication.getName()); // 로그인된 회원 아이디와 memberNo으로 조회한 회원 아이디가 동일한지 확인
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(MemberRole.ROLE_ADMIN.name()));
    }
}
