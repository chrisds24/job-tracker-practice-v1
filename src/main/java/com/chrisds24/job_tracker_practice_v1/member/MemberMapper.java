package com.chrisds24.job_tracker_practice_v1.member;

import java.util.List;

import com.chrisds24.job_tracker_practice_v1.auth.dto.LoginResponseDto;
import com.chrisds24.job_tracker_practice_v1.member.dto.MemberResponseDto;

public class MemberMapper {
    public static Member toEntity(
        String name,
        String email,
        String passwordHash,
        List<String> roles
    ) {
        return new Member(
            name,
            email,
            passwordHash,
            roles
        );
    }

    public static MemberResponseDto toMemberResponseDto(
        Member member
    ) {
        return new MemberResponseDto(
            member.getId(),
            member.getName(),
            member.getEmail(),
            member.getRoles()
        );
    }

    public static LoginResponseDto toLoginResponseDto(
        String jwt,
        Member member
    ) {
        return new LoginResponseDto(
            jwt,
            toMemberResponseDto(member)
        );
    }
}
