package com.houjiang.userms.ucenter;

import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.mapper.UserInfoMapper;
import com.houjiang.userms.ucenter.dto.UcenterRequests.InviteRequest;
import com.houjiang.userms.ucenter.mapper.UcenterInvitationMapper;
import com.houjiang.userms.ucenter.mapper.UcenterInvitationMenuMapper;
import com.houjiang.userms.ucenter.mapper.UcenterMemberMenuMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMemberMapper;
import com.houjiang.userms.ucenter.model.UcenterInvitationEntity;
import com.houjiang.userms.ucenter.model.UcenterInvitationStatus;
import com.houjiang.userms.ucenter.model.UcenterMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterMemberMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterRole;
import com.houjiang.userms.ucenter.model.UcenterSpaceEntity;
import com.houjiang.userms.ucenter.model.UcenterSpaceMemberEntity;
import com.houjiang.userms.ucenter.service.UcenterIdentityService;
import com.houjiang.userms.ucenter.service.UcenterAuditService;
import com.houjiang.userms.ucenter.service.UcenterInvitationService;
import com.houjiang.userms.ucenter.service.UcenterPermissionService;
import com.houjiang.userms.ucenter.model.UcenterPermissionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UcenterInvitationServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-28T08:00:00Z");

    private final UcenterIdentityService identity = mock(UcenterIdentityService.class);
    private final UcenterSpaceMapper spaces = mock(UcenterSpaceMapper.class);
    private final UcenterSpaceMemberMapper members = mock(UcenterSpaceMemberMapper.class);
    private final UcenterInvitationMapper invitations = mock(UcenterInvitationMapper.class);
    private final UcenterInvitationMenuMapper invitationMenus = mock(UcenterInvitationMenuMapper.class);
    private final UcenterMemberMenuMapper memberMenus = mock(UcenterMemberMenuMapper.class);
    private final UserInfoMapper users = mock(UserInfoMapper.class);
    private final UcenterPermissionService permissions = mock(UcenterPermissionService.class);
    private final UcenterAuditService audits = mock(UcenterAuditService.class);
    private UcenterInvitationService service;

    @BeforeEach
    void setUp() {
        service = new UcenterInvitationService(identity, spaces, members, invitations,
                invitationMenus, memberMenus, users, permissions,
                audits, Clock.fixed(NOW, ZoneOffset.UTC));
        when(identity.requireUserId()).thenReturn(42L);
    }

    @Test
    void ownerInvitesAnExactNormalizedMobileForSevenDays() {
        when(members.findActiveByUserIdForUpdate(42L)).thenReturn(member(20L, 10L, 42L, UcenterRole.OWNER));
        UserInfoVO invitee = new UserInfoVO();
        invitee.setUserId("77");
        invitee.setUserMobile("13800138000");
        when(users.getUserInfoByPhoneNumber("13800138000")).thenReturn(invitee);
        when(permissions.requireConfigurableMenus(Set.of("ucenter:order:list")))
                .thenReturn(List.of(menu(4L, "ucenter:order:list")));
        when(invitations.findPendingForUpdate(10L, 77L)).thenReturn(null);

        service.invite(new InviteRequest(" 138 0013 8000 ", Set.of("ucenter:order:list")));

        verify(users).getUserInfoByPhoneNumber("13800138000");
        ArgumentCaptor<UcenterInvitationEntity> invitation = ArgumentCaptor.forClass(UcenterInvitationEntity.class);
        verify(invitations).insert(invitation.capture());
        assertEquals("8000", invitation.getValue().getMaskedMobile());
        assertEquals(LocalDateTime.ofInstant(NOW.plus(7, ChronoUnit.DAYS), ZoneOffset.UTC),
                invitation.getValue().getExpiresAt());
        assertEquals(64, invitation.getValue().getTokenDigest().length());
        verify(audits).recordSuccess(UcenterPermissionEvent.INVITATION_SENT, 10L, null,
                Set.of(), Set.of("ucenter:order:list"));
    }

    @Test
    void acceptingClosesOnlyAnEmptyPersonalSpaceAndCopiesGrants() {
        when(identity.requireUserId()).thenReturn(77L);
        UcenterInvitationEntity invitation = invitation(100L, 10L, 42L, 77L);
        when(invitations.findByIdForUpdate(100L)).thenReturn(invitation);
        when(members.findActiveByUserIdForUpdate(77L)).thenReturn(member(30L, 30L, 77L, UcenterRole.OWNER));
        when(spaces.findByIdForUpdate(30L)).thenReturn(space(30L, 77L));
        when(spaces.findByIdForUpdate(10L)).thenReturn(space(10L, 42L));
        when(members.countOtherActiveMembers(30L, 77L)).thenReturn(0L);
        when(spaces.closeActiveSpace(30L, 77L)).thenReturn(1);
        when(members.markLeft(30L, 77L)).thenReturn(1);
        when(invitationMenus.findMenuIds(100L)).thenReturn(List.of(4L));

        service.accept(100L);

        verify(spaces).closeActiveSpace(30L, 77L);
        verify(members).markLeft(30L, 77L);
        verify(memberMenus).insert(any(UcenterMemberMenuEntity.class));
        verify(audits).recordSuccess(UcenterPermissionEvent.INVITATION_ACCEPTED, 10L, null,
                Set.of(), Set.of());
        assertEquals(UcenterInvitationStatus.ACCEPTED, invitation.getStatus());
    }

    @Test
    void ownerWithMembersCannotAcceptAnotherInvitation() {
        when(identity.requireUserId()).thenReturn(77L);
        when(invitations.findByIdForUpdate(100L)).thenReturn(invitation(100L, 10L, 42L, 77L));
        when(members.findActiveByUserIdForUpdate(77L)).thenReturn(member(30L, 30L, 77L, UcenterRole.OWNER));
        when(spaces.findByIdForUpdate(30L)).thenReturn(space(30L, 77L));
        when(members.countOtherActiveMembers(30L, 77L)).thenReturn(2L);

        assertThrows(RuntimeException.class, () -> service.accept(100L));
    }

    @Test
    void expiredInvitationUsesANonRollbackExceptionSoStatusAndAuditPersist() throws Exception {
        when(identity.requireUserId()).thenReturn(77L);
        UcenterInvitationEntity expired = invitation(100L, 10L, 42L, 77L);
        expired.setExpiresAt(LocalDateTime.ofInstant(NOW.minus(1, ChronoUnit.MINUTES), ZoneOffset.UTC));
        when(invitations.findByIdForUpdate(100L)).thenReturn(expired);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> service.accept(100L));

        assertEquals("UcenterInvitationExpiredException", thrown.getClass().getSimpleName());
        assertEquals(UcenterInvitationStatus.EXPIRED, expired.getStatus());
        verify(invitations).updateById(expired);
        verify(audits).recordSuccess(UcenterPermissionEvent.INVITATION_EXPIRED, 10L, null,
                Set.of("status:PENDING"), Set.of("status:EXPIRED"));
        Transactional transaction = UcenterInvitationService.class.getMethod("accept", Long.class)
                .getAnnotation(Transactional.class);
        assertEquals("UcenterInvitationExpiredException", transaction.noRollbackFor()[0].getSimpleName());
    }

    private static UcenterInvitationEntity invitation(Long id, Long spaceId, Long inviter, Long invitee) {
        UcenterInvitationEntity row = new UcenterInvitationEntity();
        row.setId(id);
        row.setSpaceId(spaceId);
        row.setInviterUserId(inviter);
        row.setInviteeUserId(invitee);
        row.setStatus(UcenterInvitationStatus.PENDING);
        row.setExpiresAt(LocalDateTime.ofInstant(NOW.plus(7, ChronoUnit.DAYS), ZoneOffset.UTC));
        return row;
    }

    private static UcenterSpaceMemberEntity member(Long id, Long spaceId, Long userId, UcenterRole role) {
        UcenterSpaceMemberEntity row = new UcenterSpaceMemberEntity();
        row.setId(id);
        row.setSpaceId(spaceId);
        row.setUserId(userId);
        row.setRole(role);
        row.setStatus("ACTIVE");
        return row;
    }

    private static UcenterSpaceEntity space(Long id, Long ownerId) {
        UcenterSpaceEntity row = new UcenterSpaceEntity();
        row.setId(id);
        row.setOwnerUserId(ownerId);
        row.setStatus(1);
        row.setVersion(1L);
        return row;
    }

    private static UcenterMenuEntity menu(Long id, String code) {
        UcenterMenuEntity row = new UcenterMenuEntity();
        row.setId(id);
        row.setCode(code);
        return row;
    }
}
