package com.houjiang.userms.ucenter.vo;

import java.util.List;

public record UcenterMenuVO(
        String code,
        String nameKey,
        String routePath,
        String icon,
        boolean mandatory,
        boolean ownerOnly,
        List<UcenterMenuVO> children
) {
    public UcenterMenuVO {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
