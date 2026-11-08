package com.houjiang.userms.ucenter.controller;

import com.houjiang.commons.annnotation.AccessLimit;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.domain.AuthErrorCode;
import com.houjiang.commons.errorcode.domain.PermissionErrorCode;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.ucenter.dto.UcenterRequests.AccessCheckRequest;
import com.houjiang.userms.ucenter.dto.UcenterRequests.InviteRequest;
import com.houjiang.userms.ucenter.dto.UcenterRequests.MenuGrantRequest;
import com.houjiang.userms.ucenter.dto.UcenterRequests.TransferOwnershipRequest;
import com.houjiang.userms.ucenter.service.UcenterAuditService;
import com.houjiang.userms.ucenter.service.UcenterDomainException;
import com.houjiang.userms.ucenter.service.UcenterInvitationService;
import com.houjiang.userms.ucenter.service.UcenterPermissionService;
import com.houjiang.userms.ucenter.service.UcenterSpaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/ucenter")
public class UcenterController {
    private final UcenterSpaceService spaceService;
    private final UcenterPermissionService permissionService;
    private final UcenterInvitationService invitationService;
    private final UcenterAuditService auditService;

    public UcenterController(UcenterSpaceService spaceService,
                             UcenterPermissionService permissionService,
                             UcenterInvitationService invitationService,
                             UcenterAuditService auditService) {
        this.spaceService = spaceService;
        this.permissionService = permissionService;
        this.invitationService = invitationService;
        this.auditService = auditService;
    }

    @GetMapping("/bootstrap")
    public ResponseResult<?> bootstrap() {
        return ResponseResult.success(spaceService.bootstrapForCurrentUser());
    }

    @PostMapping("/page-access/check")
    public ResponseResult<Boolean> checkPageAccess(@Valid @RequestBody AccessCheckRequest request) {
        return ResponseResult.success(permissionService.mayCurrentUserAccess(request.permissionCode()));
    }

    @GetMapping("/space/members")
    public ResponseResult<?> members() {
        return ResponseResult.success(spaceService.membersForOwner());
    }

    @GetMapping("/space/audits")
    public ResponseResult<?> audits(@RequestParam(defaultValue = "50") int limit) {
        return ResponseResult.success(auditService.recentForOwner(limit));
    }

    @AccessLimit
    @PostMapping("/invitations")
    public ResponseResult<?> invite(@Valid @RequestBody InviteRequest request) {
        return ResponseResult.success(invitationService.invite(request));
    }

    @GetMapping("/invitations/pending")
    public ResponseResult<?> pendingInvitations() {
        return ResponseResult.success(invitationService.pendingForCurrentUser());
    }

    @PostMapping("/invitations/{id}/accept")
    public ResponseResult<Void> acceptInvitation(@PathVariable Long id) {
        invitationService.accept(id);
        return ResponseResult.success();
    }

    @PostMapping("/invitations/{id}/reject")
    public ResponseResult<Void> rejectInvitation(@PathVariable Long id) {
        invitationService.reject(id);
        return ResponseResult.success();
    }

    @PostMapping("/invitations/{id}/cancel")
    public ResponseResult<Void> cancelInvitation(@PathVariable Long id) {
        invitationService.cancel(id);
        return ResponseResult.success();
    }

    @PutMapping("/space/members/{memberId}/menus")
    public ResponseResult<Void> replaceMemberMenus(@PathVariable Long memberId,
                                                    @Valid @RequestBody MenuGrantRequest request) {
        permissionService.replaceMemberMenus(memberId, request.menuCodes());
        return ResponseResult.success();
    }

    @DeleteMapping("/space/members/{memberId}")
    public ResponseResult<Void> removeMember(@PathVariable Long memberId) {
        spaceService.removeMember(memberId);
        return ResponseResult.success();
    }

    @PostMapping("/space/ownership/transfer")
    public ResponseResult<Void> transferOwnership(@Valid @RequestBody TransferOwnershipRequest request) {
        spaceService.transferOwnership(request.memberId());
        return ResponseResult.success();
    }

    @PostMapping("/space/leave")
    public ResponseResult<Void> leaveSpace() {
        spaceService.leaveCurrentSpace();
        return ResponseResult.success();
    }

    @ExceptionHandler(UcenterDomainException.class)
    public ResponseResult<Void> handleDomainException(UcenterDomainException exception) {
        ErrorCodeDef code = switch (exception.kind()) {
            case UNAUTHENTICATED -> AuthErrorCode.USER_NOT_LOGGED_IN;
            case FORBIDDEN -> PermissionErrorCode.PERMISSION_ACCESS_DENIED;
            case NOT_FOUND -> PermissionErrorCode.UCENTER_RESOURCE_NOT_FOUND;
            case CONFLICT -> PermissionErrorCode.UCENTER_SPACE_CONFLICT;
            case EXPIRED -> PermissionErrorCode.UCENTER_INVITATION_EXPIRED;
            case INVALID_TRANSITION -> PermissionErrorCode.UCENTER_INVITATION_TRANSITION_INVALID;
            case INVALID_INPUT -> PermissionErrorCode.PERMISSION_PARAM_INVALID;
        };
        return ResponseResult.fail(code.code(), exception.getMessage());
    }
}
