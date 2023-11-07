package com.hihat.membertemplate.member.controller;

import com.hihat.membertemplate.member.config.jwt.AuthTokenManager;
import com.hihat.membertemplate.member.domain.Member;
import com.hihat.membertemplate.member.dto.*;
import com.hihat.membertemplate.member.service.MemberService;
import com.hihat.membertemplate.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    private final AuthTokenManager authTokenManager;
    private final ModelMapper modelMapper;

    @PostMapping("/login")
    public ResponseEntity<CreateAccessTokenResponse> Login(@RequestBody LoginMemberRequest loginInfo,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {

        Member member = memberService.login(loginInfo.getMemberId(), loginInfo.getMemberPw());
        String accessToken = authTokenManager.progressAuthenticationTokenIssuance(request, response, member);

        return ResponseEntity.ok()
                .body(new CreateAccessTokenResponse(accessToken));
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody AddMemberRequest request) {

        memberService.signup(modelMapper.map(request, Member.class));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request, response, AuthTokenManager.REFRESH_TOKEN_COOKIE_NAME);
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    }

    @GetMapping("/users/{memberNo}")
    @PreAuthorize("@memberService.isAdminOrSelf(#memberNo)")
    public FindMemberResponse findMember(@PathVariable long memberNo) {

        return modelMapper.map(memberService.findByMemberNo(memberNo), FindMemberResponse.class);
    }

    @PutMapping("/users/{memberNo}")
    @PreAuthorize("@memberService.isAdminOrSelf(#memberNo)")
    public void editMemberInfo(@RequestBody EditMemberInfoRequest request,
                             @PathVariable long memberNo) {

        memberService.editMember(memberNo, modelMapper.map(request, Member.class));
    }

    @DeleteMapping("/users/{memberNo}")
    @PreAuthorize("@memberService.isAdminOrSelf(#memberNo)")
    public void removeMember(@PathVariable long memberNo) {

        memberService.removeMember(memberNo);
    }
}
