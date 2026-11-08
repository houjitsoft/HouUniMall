package com.houjiang.userms.ucenter;

import com.houjiang.userms.ucenter.mapper.UcenterInvitationMapper;
import com.houjiang.userms.ucenter.mapper.UcenterMemberMenuMapper;
import com.houjiang.userms.ucenter.mapper.UcenterMenuMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMemberMapper;
import com.houjiang.userms.ucenter.model.UcenterMemberMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterRole;
import com.houjiang.userms.ucenter.model.UcenterSpaceMemberEntity;
import com.houjiang.userms.ucenter.service.UcenterPermissionService;
import com.houjiang.userms.ucenter.service.UcenterAuditService;
import com.houjiang.userms.ucenter.service.UcenterDomainException;
import com.houjiang.userms.ucenter.service.UcenterIdentityService;
import com.houjiang.userms.ucenter.vo.UcenterMenuVO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UcenterPermissionServiceTest {

    private final UcenterMenuMapper menus = mock(UcenterMenuMapper.class);
    private final UcenterMemberMenuMapper grants = mock(UcenterMemberMenuMapper.class);
    private final UcenterInvitationMapper invitations = mock(UcenterInvitationMapper.class);
    private final UcenterPermissionService service = new UcenterPermissionService(menus, grants, invitations);

    @Test
    void memberReceivesMandatoryAndExplicitLeavesOnly() {
        UcenterSpaceMemberEntity member = member(20L, UcenterRole.MEMBER);
        when(menus.findEnabledVisible()).thenReturn(List.of(
                menu(1L, "ucenter:group:member", null, false, false),
                menu(2L, "ucenter:member:security", "ucenter:group:member", true, false),
                menu(3L, "ucenter:group:orders", null, false, false),
                menu(4L, "ucenter:order:list", "ucenter:group:orders", false, false),
                menu(5L, "ucenter:permissions:manage", "ucenter:group:member", false, true)));
        when(grants.findGrantedMenuIds(20L)).thenReturn(Set.of(4L));

        List<UcenterMenuVO> tree = service.effectiveMenuTree(member);

        assertEquals(List.of("ucenter:group:member", "ucenter:group:orders"),
                tree.stream().map(UcenterMenuVO::code).toList());
        assertEquals(List.of("ucenter:member:security"),
                tree.get(0).children().stream().map(UcenterMenuVO::code).toList());
        assertEquals(List.of("ucenter:order:list"),
                tree.get(1).children().stream().map(UcenterMenuVO::code).toList());
        assertFalse(service.mayAccess(member, "ucenter:permissions:manage"));
        assertTrue(service.mayAccess(member, "ucenter:order:list"));
    }

    @Test
    void ownerReceivesEveryEnabledLeafIncludingOwnerManagement() {
        UcenterSpaceMemberEntity owner = member(20L, UcenterRole.OWNER);
        when(menus.findEnabledVisible()).thenReturn(List.of(
                menu(1L, "ucenter:group:permissions", null, false, true),
                menu(2L, "ucenter:permissions:manage", "ucenter:group:permissions", false, true)));

        assertTrue(service.mayAccess(owner, "ucenter:permissions:manage"));
        assertEquals("ucenter:permissions:manage",
                service.effectiveMenuTree(owner).get(0).children().get(0).code());
    }

    @Test
    void unknownAndDisabledCodesFailClosed() {
        UcenterSpaceMemberEntity owner = member(20L, UcenterRole.OWNER);
        when(menus.findEnabledVisible()).thenReturn(List.of());

        assertFalse(service.mayAccess(owner, "ucenter:server:unknown"));
    }

    @Test
    void mapperFilteredMenuRemainsConfigurableWhenFilterColumnsAreNotMapped() {
        UcenterMenuEntity order = menu(
                4L, "ucenter:order:list", "ucenter:group:orders", false, false);
        order.setShow(null);
        order.setEnabled(null);
        when(menus.findEnabledByCodes(Set.of("ucenter:order:list")))
                .thenReturn(List.of(order));

        assertEquals(List.of("ucenter:order:list"),
                service.requireConfigurableMenus(Set.of("ucenter:order:list")).stream()
                        .map(UcenterMenuEntity::getCode)
                        .toList());
    }

    @Test
    void personalOwnerCannotAccessEnterprisePageDespiteMenuGrant() {
        UcenterIdentityService identity = mock(UcenterIdentityService.class);
        UcenterSpaceMemberMapper members = mock(UcenterSpaceMemberMapper.class);
        UcenterPermissionService managed = new UcenterPermissionService(
                menus, grants, invitations, identity, members, mock(UcenterSpaceMapper.class),
                mock(UcenterAuditService.class));
        when(identity.requireCurrentUser()).thenReturn(currentUser(false));
        when(members.findActiveByUserId(42L)).thenReturn(member(20L, UcenterRole.OWNER));
        when(menus.findEnabledVisible()).thenReturn(enterpriseCatalog());

        assertFalse(managed.mayCurrentUserAccess("ucenter:enterprise:profile"));
    }

    @Test
    void businessMemberEnterpriseAccessFollowsMenuGrant() {
        UcenterIdentityService identity = mock(UcenterIdentityService.class);
        UcenterSpaceMemberMapper members = mock(UcenterSpaceMemberMapper.class);
        UcenterPermissionService managed = new UcenterPermissionService(
                menus, grants, invitations, identity, members, mock(UcenterSpaceMapper.class),
                mock(UcenterAuditService.class));
        when(identity.requireCurrentUser()).thenReturn(currentUser(true));
        when(members.findActiveByUserId(42L)).thenReturn(member(20L, UcenterRole.MEMBER));
        when(menus.findEnabledVisible()).thenReturn(enterpriseCatalog());
        when(grants.findGrantedMenuIds(20L)).thenReturn(Set.of(2L), Set.of());

        assertTrue(managed.mayCurrentUserAccess("ucenter:enterprise:profile"));
        assertFalse(managed.mayCurrentUserAccess("ucenter:enterprise:profile"));
    }

    @Test
    void personalOwnerRetainsOrdinaryMenuAccess() {
        UcenterIdentityService identity = mock(UcenterIdentityService.class);
        UcenterSpaceMemberMapper members = mock(UcenterSpaceMemberMapper.class);
        UcenterPermissionService managed = new UcenterPermissionService(
                menus, grants, invitations, identity, members, mock(UcenterSpaceMapper.class),
                mock(UcenterAuditService.class));
        when(identity.requireCurrentUser()).thenReturn(currentUser(false));
        when(members.findActiveByUserId(42L)).thenReturn(member(20L, UcenterRole.OWNER));
        when(menus.findEnabledVisible()).thenReturn(List.of(
                menu(3L, "ucenter:group:orders", null, false, false),
                menu(4L, "ucenter:order:list", "ucenter:group:orders", false, false)));

        assertTrue(managed.mayCurrentUserAccess("ucenter:order:list"));
    }

    @Test
    void normalMemberCannotReplaceAnotherMembersMenus() {
        UcenterIdentityService identity = mock(UcenterIdentityService.class);
        UcenterSpaceMemberMapper members = mock(UcenterSpaceMemberMapper.class);
        UcenterSpaceMapper spaces = mock(UcenterSpaceMapper.class);
        UcenterAuditService audits = mock(UcenterAuditService.class);
        UcenterPermissionService managed = new UcenterPermissionService(
                menus, grants, invitations, identity, members, spaces, audits);
        when(identity.requireUserId()).thenReturn(42L);
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(member(20L, UcenterRole.MEMBER));

        assertThrows(UcenterDomainException.class,
                () -> managed.replaceMemberMenus(30L, Set.of("ucenter:order:list")));
        verify(grants, never()).deleteByMemberId(any());
    }

    @Test
    void ownerReplacesSameSpaceMemberMenusAndIncrementsVersion() {
        UcenterIdentityService identity = mock(UcenterIdentityService.class);
        UcenterSpaceMemberMapper members = mock(UcenterSpaceMemberMapper.class);
        UcenterSpaceMapper spaces = mock(UcenterSpaceMapper.class);
        UcenterAuditService audits = mock(UcenterAuditService.class);
        UcenterPermissionService managed = new UcenterPermissionService(
                menus, grants, invitations, identity, members, spaces, audits);
        UcenterSpaceMemberEntity owner = member(20L, UcenterRole.OWNER);
        UcenterSpaceMemberEntity target = member(30L, UcenterRole.MEMBER);
        target.setUserId(77L);
        when(identity.requireUserId()).thenReturn(42L);
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(owner);
        when(members.findActiveByIdForUpdate(30L)).thenReturn(target);
        when(menus.findEnabledByCodes(Set.of("ucenter:order:list")))
                .thenReturn(List.of(menu(4L, "ucenter:order:list", "ucenter:group:orders", false, false)));
        when(grants.findGrantedMenuIds(30L)).thenReturn(Set.of());

        managed.replaceMemberMenus(30L, Set.of("ucenter:order:list"));

        verify(grants).insert(any(UcenterMemberMenuEntity.class));
        verify(spaces).incrementVersion(10L);
        verify(audits).recordSuccess(any(), any(), any(), any(), any());
    }

    @Test
    void ownerCannotReplaceMenusForMemberInAnotherSpace() {
        UcenterIdentityService identity = mock(UcenterIdentityService.class);
        UcenterSpaceMemberMapper members = mock(UcenterSpaceMemberMapper.class);
        UcenterSpaceMapper spaces = mock(UcenterSpaceMapper.class);
        UcenterAuditService audits = mock(UcenterAuditService.class);
        UcenterPermissionService managed = new UcenterPermissionService(
                menus, grants, invitations, identity, members, spaces, audits);
        UcenterSpaceMemberEntity owner = member(20L, UcenterRole.OWNER);
        UcenterSpaceMemberEntity outside = member(30L, UcenterRole.MEMBER);
        outside.setSpaceId(99L);
        when(identity.requireUserId()).thenReturn(42L);
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(owner);
        when(members.findActiveByIdForUpdate(30L)).thenReturn(outside);

        assertThrows(UcenterDomainException.class,
                () -> managed.replaceMemberMenus(30L, Set.of("ucenter:order:list")));

        verify(grants, never()).deleteByMemberId(any());
        verify(spaces, never()).incrementVersion(any());
    }

    private static UcenterSpaceMemberEntity member(Long id, UcenterRole role) {
        UcenterSpaceMemberEntity row = new UcenterSpaceMemberEntity();
        row.setId(id);
        row.setSpaceId(10L);
        row.setUserId(42L);
        row.setRole(role);
        row.setStatus("ACTIVE");
        return row;
    }

    private static UcenterIdentityService.CurrentUser currentUser(boolean business) {
        return new UcenterIdentityService.CurrentUser(42L, "会员42", "1234", null, business);
    }

    private static List<UcenterMenuEntity> enterpriseCatalog() {
        return List.of(
                menu(1L, "ucenter:group:enterprise", null, false, false),
                menu(2L, "ucenter:enterprise:profile", "ucenter:group:enterprise", false, false));
    }

    private static UcenterMenuEntity menu(Long id, String code, String parent, boolean mandatory, boolean ownerOnly) {
        UcenterMenuEntity row = new UcenterMenuEntity();
        row.setId(id);
        row.setCode(code);
        row.setParentCode(parent);
        row.setNameKey(code + ".name");
        row.setRoutePath("/" + code.replace(':', '/'));
        row.setSort(100 - id.intValue());
        row.setMandatory(mandatory);
        row.setOwnerOnly(ownerOnly);
        row.setShow(true);
        row.setEnabled(true);
        return row;
    }
}
