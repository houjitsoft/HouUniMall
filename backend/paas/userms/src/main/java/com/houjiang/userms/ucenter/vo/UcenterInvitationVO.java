package com.houjiang.userms.ucenter.vo;

import com.houjiang.userms.ucenter.model.UcenterInvitationEntity;
import com.houjiang.userms.ucenter.model.UcenterInvitationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record UcenterInvitationVO(
        Long id,
        Long spaceId,
        String maskedMobile,
        UcenterInvitationStatus status,
        LocalDateTime expiresAt,
        List<String> menuCodes
) {
    public static UcenterInvitationVO from(UcenterInvitationEntity entity, List<String> menuCodes) {
        return new UcenterInvitationVO(entity.getId(), entity.getSpaceId(), entity.getMaskedMobile(),
                entity.getStatus(), entity.getExpiresAt(), menuCodes == null ? List.of() : List.copyOf(menuCodes));
    }
}
