package com.houjiang.userms.ucenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public final class UcenterRequests {
    private UcenterRequests() {}

    public record AccessCheckRequest(@NotBlank @Size(max = 100) String permissionCode) {}

    public record InviteRequest(
            @NotBlank @Size(max = 32) String mobile,
            @NotNull Set<@NotBlank @Size(max = 100) String> menuCodes
    ) {
        public InviteRequest {
            menuCodes = menuCodes == null ? Set.of() : Set.copyOf(menuCodes);
        }
    }

    public record MenuGrantRequest(@NotNull Set<@NotBlank @Size(max = 100) String> menuCodes) {
        public MenuGrantRequest {
            menuCodes = menuCodes == null ? Set.of() : Set.copyOf(menuCodes);
        }
    }

    public record TransferOwnershipRequest(@NotNull Long memberId) {}
}
