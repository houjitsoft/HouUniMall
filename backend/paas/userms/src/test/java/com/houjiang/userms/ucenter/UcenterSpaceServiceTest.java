package com.houjiang.userms.ucenter;

import com.houjiang.userms.ucenter.mapper.UcenterSpaceMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMemberMapper;
import com.houjiang.userms.ucenter.mapper.UcenterMemberMenuMapper;
import com.houjiang.userms.ucenter.model.UcenterRole;
import com.houjiang.userms.ucenter.model.UcenterMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterPermissionEvent;
import com.houjiang.userms.ucenter.model.UcenterSpaceEntity;
import com.houjiang.userms.ucenter.model.UcenterSpaceMemberEntity;
import com.houjiang.userms.ucenter.service.UcenterIdentityService;
import com.houjiang.userms.ucenter.service.UcenterPermissionService;
import com.houjiang.userms.ucenter.service.UcenterAuditService;
import com.houjiang.userms.ucenter.service.UcenterSpaceService;
import com.houjiang.userms.ucenter.service.UcenterDomainException;
import com.houjiang.userms.ucenter.vo.UcenterBootstrapVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import org.mockito.InOrder;

class UcenterSpaceServiceTest {

    private final UcenterIdentityService identity = mock(UcenterIdentityService.class);
    private final UcenterSpaceMapper spaces = mock(UcenterSpaceMapper.class);
    private final UcenterSpaceMemberMapper members = mock(UcenterSpaceMemberMapper.class);
    private final UcenterPermissionService permissions = mock(UcenterPermissionService.class);
    private UcenterSpaceService service;

    @BeforeEach
    void setUp() {
        service = new UcenterSpaceService(identity, spaces, members, permissions);
        when(identity.requireCurrentUser()).thenReturn(
                new UcenterIdentityService.CurrentUser(42L, "林野", "8000", null, false));
        when(permissions.effectiveMenuTree(any())).thenReturn(List.of());
        when(permissions.pendingInvitationCount(42L)).thenReturn(0L);
    }

    @Test
    void bootstrapCreatesOneOwnerSpaceWhenMembershipIsMissing() {
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(null);
        doAnswer(invocation -> {
            UcenterSpaceEntity row = invocation.getArgument(0);
            row.setId(10L);
            return 1;
        }).when(spaces).insert(any(UcenterSpaceEntity.class));
        doAnswer(invocation -> {
            UcenterSpaceMemberEntity row = invocation.getArgument(0);
            row.setId(20L);
            return 1;
        }).when(members).insert(any(UcenterSpaceMemberEntity.class));

        UcenterBootstrapVO result = service.bootstrapForCurrentUser();

        assertEquals(UcenterRole.OWNER, result.role());
        assertEquals(10L, result.space().id());
        assertEquals("林野的个人空间", result.space().name());
        verify(spaces).insert(any(UcenterSpaceEntity.class));
        verify(members).insert(any(UcenterSpaceMemberEntity.class));
    }

    @Test
    void duplicateSpaceCreationReloadsTheWinningMembership() {
        UcenterSpaceMemberEntity winner = activeOwner(20L, 10L, 42L);
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(null);
        when(members.findActiveByUserId(42L)).thenReturn(winner);
        when(spaces.selectById(10L)).thenReturn(activeSpace(10L, 42L, "林野的个人空间"));
        doThrow(new DuplicateKeyException("active owner already exists")).when(spaces).insert(any(UcenterSpaceEntity.class));

        UcenterBootstrapVO result = service.bootstrapForCurrentUser();

        assertEquals(10L, result.space().id());
        verify(members, never()).insert(any(UcenterSpaceMemberEntity.class));
    }

    @Test
    void leavingMemberReceivesANewPersonalSpace() {
        UcenterMemberMenuMapper grants = mock(UcenterMemberMenuMapper.class);
        UcenterAuditService audits = mock(UcenterAuditService.class);
        service = new UcenterSpaceService(identity, spaces, members, permissions, grants, audits);
        UcenterSpaceMemberEntity member = activeOwner(30L, 10L, 42L);
        member.setRole(UcenterRole.MEMBER);
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(member);
        when(members.markLeft(30L, 42L)).thenReturn(1);
        doAnswer(invocation -> {
            UcenterSpaceEntity row = invocation.getArgument(0);
            row.setId(40L);
            return 1;
        }).when(spaces).insert(any(UcenterSpaceEntity.class));

        service.leaveCurrentSpace();

        verify(members).markLeft(30L, 42L);
        verify(members).insert(any(UcenterSpaceMemberEntity.class));
        verify(spaces).incrementVersion(10L);
        verify(audits).recordSuccess(UcenterPermissionEvent.MEMBER_LEFT, 10L, 30L,
                Set.of(), Set.of());
    }

    @Test
    void ownerCannotLeaveWhileOtherMembersRemain() {
        UcenterMemberMenuMapper grants = mock(UcenterMemberMenuMapper.class);
        UcenterAuditService audits = mock(UcenterAuditService.class);
        service = new UcenterSpaceService(identity, spaces, members, permissions, grants, audits);
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(activeOwner(30L, 10L, 42L));
        when(members.countOtherActiveMembers(10L, 42L)).thenReturn(1L);

        assertThrows(UcenterDomainException.class, service::leaveCurrentSpace);

        verify(members, never()).markLeft(any(), any());
        verify(spaces, never()).insert(any(UcenterSpaceEntity.class));
    }

    @Test
    void ownerRemovalMovesMemberIntoExactlyOnePersonalOwnerSpace() {
        UcenterMemberMenuMapper grants = mock(UcenterMemberMenuMapper.class);
        UcenterAuditService audits = mock(UcenterAuditService.class);
        service = new UcenterSpaceService(identity, spaces, members, permissions, grants, audits);
        UcenterSpaceMemberEntity owner = activeOwner(20L, 10L, 42L);
        UcenterSpaceMemberEntity target = activeOwner(30L, 10L, 77L);
        target.setRole(UcenterRole.MEMBER);
        when(identity.requireUserId()).thenReturn(42L);
        when(identity.requireCurrentUser()).thenReturn(
                new UcenterIdentityService.CurrentUser(42L, "林野", "8000", null, false));
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(owner);
        when(members.findActiveByIdForUpdate(30L)).thenReturn(target);
        when(grants.findGrantedMenuCodes(30L)).thenReturn(Set.of("ucenter:order:list"));
        when(members.markLeft(30L, 77L)).thenReturn(1);
        doAnswer(invocation -> {
            UcenterSpaceEntity row = invocation.getArgument(0);
            row.setId(40L);
            return 1;
        }).when(spaces).insert(any(UcenterSpaceEntity.class));

        service.removeMember(30L);

        verify(members).insert(org.mockito.ArgumentMatchers.argThat((UcenterSpaceMemberEntity member) ->
                member.getSpaceId().equals(40L)
                        && member.getUserId().equals(77L)
                        && member.getRole() == UcenterRole.OWNER));
        verify(spaces).incrementVersion(10L);
        verify(audits).recordSuccess(UcenterPermissionEvent.MEMBER_REMOVED, 10L, 30L,
                Set.of("ucenter:order:list"), Set.of());
    }

    @Test
    void ownershipTransferDemotesOwnerBeforePromotingTargetAndAuditsBothRoles() {
        UcenterMemberMenuMapper grants = mock(UcenterMemberMenuMapper.class);
        UcenterAuditService audits = mock(UcenterAuditService.class);
        service = new UcenterSpaceService(identity, spaces, members, permissions, grants, audits);
        UcenterSpaceMemberEntity owner = activeOwner(20L, 10L, 42L);
        UcenterSpaceMemberEntity target = activeOwner(30L, 10L, 77L);
        target.setRole(UcenterRole.MEMBER);
        UcenterMenuEntity menu = new UcenterMenuEntity();
        menu.setId(4L);
        menu.setCode("ucenter:order:list");
        when(identity.requireUserId()).thenReturn(42L);
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(owner);
        when(members.findActiveByIdForUpdate(30L)).thenReturn(target);
        when(members.updateRole(20L, "MEMBER")).thenReturn(1);
        when(members.updateRole(30L, "OWNER")).thenReturn(1);
        when(spaces.transferOwner(10L, 42L, 77L)).thenReturn(1);
        when(permissions.configurableCatalog()).thenReturn(List.of(menu));

        service.transferOwnership(30L);

        InOrder ordered = inOrder(members, spaces);
        ordered.verify(members).updateRole(20L, "MEMBER");
        ordered.verify(members).updateRole(30L, "OWNER");
        ordered.verify(spaces).transferOwner(10L, 42L, 77L);
        verify(audits).recordSuccess(UcenterPermissionEvent.OWNERSHIP_TRANSFERRED, 10L, 30L,
                Set.of("member:20:OWNER", "member:30:MEMBER"),
                Set.of("member:20:MEMBER", "member:30:OWNER"));
    }

    private static UcenterSpaceMemberEntity activeOwner(Long id, Long spaceId, Long userId) {
        UcenterSpaceMemberEntity row = new UcenterSpaceMemberEntity();
        row.setId(id);
        row.setSpaceId(spaceId);
        row.setUserId(userId);
        row.setRole(UcenterRole.OWNER);
        row.setStatus("ACTIVE");
        return row;
    }

    private static UcenterSpaceEntity activeSpace(Long id, Long ownerId, String name) {
        UcenterSpaceEntity row = new UcenterSpaceEntity();
        row.setId(id);
        row.setOwnerUserId(ownerId);
        row.setName(name);
        row.setStatus(1);
        row.setVersion(1L);
        return row;
    }
}
