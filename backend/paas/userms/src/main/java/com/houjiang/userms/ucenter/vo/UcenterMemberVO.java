package com.houjiang.userms.ucenter.vo;

import com.houjiang.userms.ucenter.model.UcenterRole;

import java.time.LocalDateTime;
import java.util.Set;

public record UcenterMemberVO(
        Long memberId,
        Long userId,
        UcenterRole role,
        LocalDateTime joinTime,
        Set<String> menuCodes
) {}
