package com.hihat.membertemplate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hihat.membertemplate.member.config.jwt.AuthTokenManager;
import com.hihat.membertemplate.member.config.security.MemberRole;
import com.hihat.membertemplate.member.domain.Member;
import com.hihat.membertemplate.member.dto.AddMemberRequest;
import com.hihat.membertemplate.member.dto.CreateAccessTokenResponse;
import com.hihat.membertemplate.member.dto.EditMemberInfoRequest;
import com.hihat.membertemplate.member.dto.LoginMemberRequest;
import com.hihat.membertemplate.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MemberApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    private Member member1;
    private Member member2;
    private String testPw = "qwer1234!";

    @BeforeEach
    public void setMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @BeforeEach
    public void setInitMember() {
        member1 = Member.builder()
                .memberId("member1")
                .memberPw(bCryptPasswordEncoder.encode(testPw))
                .memberName("member1")
                .memberTel("010-1111-1111")
                .memberRole(MemberRole.ROLE_ADMIN)
                .build();

        member2 = Member.builder()
                .memberId("member2")
                .memberPw(bCryptPasswordEncoder.encode(testPw))
                .memberName("member2")
                .memberTel("010-2222-2222")
                .memberRole(MemberRole.ROLE_USER)
                .build();

        member1 = memberRepository.save(member1);
        member2 = memberRepository.save(member2);

        // member1 로그인 인증 처리
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(member1, member1.getMemberPw(), member1.getAuthorities()));
    }

    @AfterEach
    public void clearData() {
        memberRepository.deleteByMemberNo(member1.getMemberNo());
        memberRepository.deleteByMemberNo(member2.getMemberNo());
    }

    @Test
    @DisplayName("login(): 회원 로그인 인증을 처리하고 토큰 발급")
    void login() throws Exception {
        // given
        final String url = "/login";
        final String loginMemberId = member1.getMemberId();
        final String loginMemberPw = testPw;
        final LoginMemberRequest loginMemberRequest = new LoginMemberRequest(loginMemberId, loginMemberPw);
        final String requestBody = objectMapper.writeValueAsString(loginMemberRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());  // 상태 코드
        result.andExpect(cookie().exists(AuthTokenManager.REFRESH_TOKEN_COOKIE_NAME));  // 쿠키에서 리프레시 토큰 여부 확인
        String accessToken = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                CreateAccessTokenResponse.class).getAccessToken();
        assertThat(accessToken).isNotNull(); // 응답 바디에서 액세스 토큰 여부 확인
    }

    @Test
    @DisplayName("signup(): 회원 가입")
    void signup() throws Exception {
        // given
        final String url = "/signup";
        final String memberId = "signupUser";
        final String memberPw = testPw;
        final String memberName = "tester";
        final String memberTel = "010-1234-5678";
        final String memberCorp = "A";
        final String memberMemo = "description";
        final AddMemberRequest request = new AddMemberRequest(
                memberId
                ,memberPw
                ,memberName
//                ,memberTel
//                ,memberCorp
//                ,memberMemo
        );
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated()); // 상태 코드 확인

        Member foundMember = memberRepository.findByMemberId(memberId).orElse(null);
        assertThat(foundMember.getMemberId()).isEqualTo(memberId); // 저장된 아이디와 일치하는지 확인
    }

    @Test
    @DisplayName("logout(): 로그아웃 처리 - 리프레시 토큰 제거")
    void logout() throws Exception {
        // given
        final String url = "/logout";

        // when
        ResultActions result = mockMvc.perform(post(url));

        // then
        result
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(AuthTokenManager.REFRESH_TOKEN_COOKIE_NAME));    // 리프레시 토큰이 지워졌는지 확인

    }

    @Test
    @DisplayName("findMember(): 회원 조회 - 본인 정보 조회")
    void findMember() throws Exception {
        // given
        final String url = "/users/{memberNo}";
        Member me = member1;

        // when
        ResultActions result = mockMvc.perform(get(url, me.getMemberNo()));

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(me.getMemberId()));
    }

    @Test
    @DisplayName("editMemberInfo(): 본인 회원 정보 수정")
    void editMemberInfo() throws Exception {
        // given
        final String url = "/users/{memberNo}";
        final String tel = "000-0000-0000";
        Member me = member1;
        final EditMemberInfoRequest editMemberInfoRequest = new EditMemberInfoRequest(
                me.getMemberName(),
                tel
        );
        String requestBody = objectMapper.writeValueAsString(editMemberInfoRequest);

        // when
        ResultActions result = mockMvc.perform(put(url, me.getMemberNo())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());

        String afterCorp = memberRepository.findByMemberNo(me.getMemberNo())
                .orElseThrow(() -> new IllegalStateException("Member not found"))
                .getMemberTel();
        assertThat(afterCorp).isEqualTo(tel);
    }

    @Test
    @Transactional
    @DisplayName("removeMember(): 회원 탈퇴(삭제)")
    void removeMember() throws Exception {
        // given
        final String url = "/users/{memberNo}";
        final String delMemberId = "duser";
        Member tempMember = Member.builder()
                .memberId(delMemberId)
                .memberPw(testPw)
                .memberName("duser")
                .memberTel("010-1234-1234")
                .build();
        Member deleteMember = memberRepository.save(tempMember);
        
        // when
        ResultActions result = mockMvc.perform(delete(url, deleteMember.getMemberNo()));

        // then
        result.andExpect(status().isOk());
        assertThat(memberRepository.findByMemberNo(deleteMember.getMemberNo())).isEmpty();
    }
}