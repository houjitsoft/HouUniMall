package com.houjiang.userms.ucenter.service;

import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.commons.util.SecurityContextUtils;
import org.springframework.stereotype.Service;

@Service
public class UcenterIdentityService {
    public CurrentUser requireCurrentUser() {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        if (user == null || user.getUserId() == null || user.getUserId().isBlank()) {
            throw new UcenterDomainException(UcenterDomainException.Kind.UNAUTHENTICATED, "登录状态已失效");
        }
        final long userId;
        try {
            userId = Long.parseLong(user.getUserId().trim());
        } catch (NumberFormatException exception) {
            throw new UcenterDomainException(UcenterDomainException.Kind.UNAUTHENTICATED, "会员身份无效");
        }
        String displayName = firstNonBlank(user.getUserNickname(), user.getUserName(), "会员" + userId);
        return new CurrentUser(
                userId,
                displayName,
                lastFour(user.getUserMobile()),
                blankToNull(user.getUserAvatar()),
                Integer.valueOf(1).equals(user.getIsBusiness())
        );
    }

    public Long requireUserId() {
        return requireCurrentUser().userId();
    }

    private static String lastFour(String mobile) {
        if (mobile == null || mobile.isBlank()) return "";
        String normalized = mobile.trim();
        return normalized.substring(Math.max(0, normalized.length() - 4));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return "会员";
    }

    public record CurrentUser(
            Long userId,
            String displayName,
            String maskedMobile,
            String avatar,
            boolean business
    ) {}
}
