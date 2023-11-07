package com.hihat.membertemplate.member.repository;

import com.hihat.membertemplate.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByMemberNo(long memberNo);

    void deleteByMemberNo(long memberNo);
}
