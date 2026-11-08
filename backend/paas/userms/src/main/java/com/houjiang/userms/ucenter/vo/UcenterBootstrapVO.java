package com.houjiang.userms.ucenter.vo;

import com.houjiang.userms.ucenter.model.UcenterRole;

import java.util.List;

public record UcenterBootstrapVO(
        ProfileSummary profile,
        SpaceSummary space,
        UcenterRole role,
        List<UcenterMenuVO> menus,
        long pendingInvitationCount,
        long permissionVersion
) {
    public record ProfileSummary(
            Long userId,
            String displayName,
            String maskedMobile,
            String avatar,
            boolean business
    ) {}

    public record SpaceSummary(Long id, String name, Long ownerUserId, long version) {}
}
