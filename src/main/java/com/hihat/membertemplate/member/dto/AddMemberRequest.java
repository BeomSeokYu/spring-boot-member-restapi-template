package com.hihat.membertemplate.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddMemberRequest {
    private String memberId;
    private String memberPw;
    private String memberName;
}
