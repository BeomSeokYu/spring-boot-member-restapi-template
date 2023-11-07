package com.hihat.membertemplate.member.test;

import com.hihat.membertemplate.member.domain.Member;
import com.hihat.membertemplate.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitMemberData implements ApplicationRunner {

    private final MemberService memberService;

    public void init() {
        Member member1 = Member.builder()
                .memberId("qwe")
                .memberPw("123")
                .memberName("qwer")
                .memberTel("010-1234-1234")
                .build();
        memberService.signup(member1);

        Member member2 = Member.builder()
                .memberId("asd")
                .memberPw("123")
                .memberName("asdf")
                .memberTel("010-5678-5678")
                .build();
        memberService.signup(member2);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        init();
    }
}
