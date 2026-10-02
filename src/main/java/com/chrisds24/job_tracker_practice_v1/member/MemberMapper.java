package com.chrisds24.job_tracker_practice_v1.member;

public class MemberMapper {
    public static Member toEntity(
        String name,
        String email,
        String passwordHash
    ) {
        return new Member(
            name,
            email,
            passwordHash
        );
    }

    public static MemberResponseDto toMemberResponseDto(
        Member member
    ) {
        return new MemberResponseDto(
            member.getId(),
            member.getName(),
            member.getEmail()
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
