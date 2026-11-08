package com.houjiang.userms.ucenter.service;

import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.mapper.UserInfoMapper;
import com.houjiang.userms.ucenter.dto.UcenterRequests.InviteRequest;
import com.houjiang.userms.ucenter.mapper.UcenterInvitationMapper;
import com.houjiang.userms.ucenter.mapper.UcenterInvitationMenuMapper;
import com.houjiang.userms.ucenter.mapper.UcenterMemberMenuMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMemberMapper;
import com.houjiang.userms.ucenter.model.UcenterInvitationEntity;
import com.houjiang.userms.ucenter.model.UcenterInvitationMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterInvitationStatus;
import com.houjiang.userms.ucenter.model.UcenterMemberMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterPermissionEvent;
import com.houjiang.userms.ucenter.model.UcenterRole;
import com.houjiang.userms.ucenter.model.UcenterSpaceEntity;
import com.houjiang.userms.ucenter.model.UcenterSpaceMemberEntity;
import com.houjiang.userms.ucenter.vo.UcenterInvitationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

@Service
public class UcenterInvitationService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UcenterIdentityService identityService;
    private final UcenterSpaceMapper spaceMapper;
    private final UcenterSpaceMemberMapper memberMapper;
    private final UcenterInvitationMapper invitationMapper;
    private final UcenterInvitationMenuMapper invitationMenuMapper;
    private final UcenterMemberMenuMapper memberMenuMapper;
    private final UserInfoMapper userInfoMapper;
    private final UcenterPermissionService permissionService;
    private final UcenterAuditService auditService;
    private final Clock clock;

    @Autowired
    public UcenterInvitationService(UcenterIdentityService identityService,
                                    UcenterSpaceMapper spaceMapper,
                                    UcenterSpaceMemberMapper memberMapper,
                                    UcenterInvitationMapper invitationMapper,
                                    UcenterInvitationMenuMapper invitationMenuMapper,
                                    UcenterMemberMenuMapper memberMenuMapper,
                                    UserInfoMapper userInfoMapper,
                                    UcenterPermissionService permissionService,
                                    UcenterAuditService auditService) {
        this(identityService, spaceMapper, memberMapper, invitationMapper, invitationMenuMapper,
                memberMenuMapper, userInfoMapper, permissionService, auditService, Clock.systemUTC());
    }

    public UcenterInvitationService(UcenterIdentityService identityService,
                                    UcenterSpaceMapper spaceMapper,
                                    UcenterSpaceMemberMapper memberMapper,
                                    UcenterInvitationMapper invitationMapper,
                                    UcenterInvitationMenuMapper invitationMenuMapper,
                                    UcenterMemberMenuMapper memberMenuMapper,
                                    UserInfoMapper userInfoMapper,
                                    UcenterPermissionService permissionService,
                                    UcenterAuditService auditService,
                                    Clock clock) {
        this.identityService = identityService;
        this.spaceMapper = spaceMapper;
        this.memberMapper = memberMapper;
        this.invitationMapper = invitationMapper;
        this.invitationMenuMapper = invitationMenuMapper;
        this.memberMenuMapper = memberMenuMapper;
        this.userInfoMapper = userInfoMapper;
        this.permissionService = permissionService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public UcenterInvitationVO invite(InviteRequest request) {
        Long actorId = identityService.requireUserId();
        UcenterSpaceMemberEntity actor = requireActiveMembership(actorId);
        requireOwner(actor);
        String mobile = normalizeMobile(request.mobile());
        UserInfoVO target = userInfoMapper.getUserInfoByPhoneNumber(mobile);
        if (target == null || target.getUserId() == null) {
            throw new UcenterDomainException(UcenterDomainException.Kind.NOT_FOUND, "未找到该手机号对应的会员");
        }
        Long inviteeId = parseUserId(target.getUserId());
        if (actorId.equals(inviteeId)) {
            throw new UcenterDomainException(UcenterDomainException.Kind.INVALID_INPUT, "不能邀请自己");
        }
        List<UcenterMenuEntity> requestedMenus = permissionService.requireConfigurableMenus(request.menuCodes());
        LocalDateTime now = LocalDateTime.now(clock);
        UcenterInvitationEntity invitation = invitationMapper.findPendingForUpdate(actor.getSpaceId(), inviteeId);
        if (invitation == null) {
            invitation = new UcenterInvitationEntity();
            invitation.setSpaceId(actor.getSpaceId());
            invitation.setInviterUserId(actorId);
            invitation.setInviteeUserId(inviteeId);
            invitation.setCreateTime(now);
        } else {
            invitationMenuMapper.deleteByInvitationId(invitation.getId());
        }
        invitation.setMaskedMobile(lastFour(mobile));
        invitation.setTokenDigest(randomDigest());
        invitation.setStatus(UcenterInvitationStatus.PENDING);
        invitation.setExpiresAt(now.plusDays(7));
        invitation.setAcceptedAt(null);
        invitation.setUpdateTime(now);
        if (invitation.getId() == null) invitationMapper.insert(invitation);
        else invitationMapper.updateById(invitation);
        for (UcenterMenuEntity menu : requestedMenus) {
            UcenterInvitationMenuEntity relation = new UcenterInvitationMenuEntity();
            relation.setInvitationId(invitation.getId());
            relation.setMenuId(menu.getId());
            invitationMenuMapper.insert(relation);
        }
        Set<String> grantedCodes = requestedMenus.stream()
                .map(UcenterMenuEntity::getCode)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        auditService.recordSuccess(UcenterPermissionEvent.INVITATION_SENT, actor.getSpaceId(), null,
                Set.of(), grantedCodes);
        return UcenterInvitationVO.from(invitation, requestedMenus.stream().map(UcenterMenuEntity::getCode).toList());
    }

    public List<UcenterInvitationVO> pendingForCurrentUser() {
        Long userId = identityService.requireUserId();
        LocalDateTime now = LocalDateTime.now(clock);
        return invitationMapper.findPendingByInvitee(userId).stream()
                .filter(row -> row.getExpiresAt() != null && row.getExpiresAt().isAfter(now))
                .map(row -> UcenterInvitationVO.from(row, List.of()))
                .toList();
    }

    @Transactional(noRollbackFor = UcenterInvitationExpiredException.class)
    public void accept(Long invitationId) {
        Long actorId = identityService.requireUserId();
        UcenterInvitationEntity invitation = requirePendingInvitation(invitationId, actorId);
        UcenterSpaceMemberEntity current = memberMapper.findActiveByUserIdForUpdate(actorId);
        if (current != null) {
            if (current.getSpaceId().equals(invitation.getSpaceId())) {
                throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "已加入该权限空间");
            }
            if (current.getRole() != UcenterRole.OWNER) {
                throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "请先退出当前权限空间");
            }
            UcenterSpaceEntity personal = spaceMapper.findByIdForUpdate(current.getSpaceId());
            if (personal == null || !actorId.equals(personal.getOwnerUserId())
                    || memberMapper.countOtherActiveMembers(personal.getId(), actorId) > 0) {
                throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT,
                        "当前空间仍有成员，请先转让所有权或移除成员");
            }
            if (spaceMapper.closeActiveSpace(personal.getId(), actorId) != 1
                    || memberMapper.markLeft(current.getId(), actorId) != 1) {
                throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "个人空间状态已变化，请重试");
            }
        }
        UcenterSpaceEntity targetSpace = spaceMapper.findByIdForUpdate(invitation.getSpaceId());
        if (targetSpace == null || !Integer.valueOf(1).equals(targetSpace.getStatus())) {
            throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "邀请空间已关闭");
        }
        UcenterSpaceMemberEntity member = new UcenterSpaceMemberEntity();
        LocalDateTime now = LocalDateTime.now(clock);
        member.setSpaceId(invitation.getSpaceId());
        member.setUserId(actorId);
        member.setRole(UcenterRole.MEMBER);
        member.setStatus("ACTIVE");
        member.setJoinedBy(invitation.getInviterUserId());
        member.setJoinTime(now);
        member.setCreateTime(now);
        member.setUpdateTime(now);
        memberMapper.insert(member);
        for (Long menuId : invitationMenuMapper.findMenuIds(invitationId)) {
            UcenterMemberMenuEntity grant = new UcenterMemberMenuEntity();
            grant.setMemberId(member.getId());
            grant.setMenuId(menuId);
            grant.setGrantedBy(invitation.getInviterUserId());
            grant.setCreateTime(now);
            memberMenuMapper.insert(grant);
        }
        invitation.setStatus(UcenterInvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(now);
        invitation.setUpdateTime(now);
        invitationMapper.updateById(invitation);
        spaceMapper.incrementVersion(invitation.getSpaceId());
        auditService.recordSuccess(UcenterPermissionEvent.INVITATION_ACCEPTED,
                invitation.getSpaceId(), null, Set.of(), Set.of());
    }

    @Transactional(noRollbackFor = UcenterInvitationExpiredException.class)
    public void reject(Long invitationId) {
        transitionInvitee(invitationId, UcenterInvitationStatus.REJECTED);
    }

    @Transactional(noRollbackFor = UcenterInvitationExpiredException.class)
    public void cancel(Long invitationId) {
        Long actorId = identityService.requireUserId();
        UcenterInvitationEntity invitation = invitationMapper.findByIdForUpdate(invitationId);
        if (invitation == null) throw notFound();
        UcenterSpaceMemberEntity actor = requireActiveMembership(actorId);
        requireOwner(actor);
        if (!actor.getSpaceId().equals(invitation.getSpaceId())) {
            throw new UcenterDomainException(UcenterDomainException.Kind.FORBIDDEN, "无权取消该邀请");
        }
        ensurePendingAndFresh(invitation);
        invitation.setStatus(UcenterInvitationStatus.CANCELLED);
        invitation.setUpdateTime(LocalDateTime.now(clock));
        invitationMapper.updateById(invitation);
        auditService.recordSuccess(UcenterPermissionEvent.INVITATION_CANCELLED,
                invitation.getSpaceId(), null,
                Set.of("status:PENDING"), Set.of("status:CANCELLED"));
    }

    private void transitionInvitee(Long invitationId, UcenterInvitationStatus status) {
        Long actorId = identityService.requireUserId();
        UcenterInvitationEntity invitation = requirePendingInvitation(invitationId, actorId);
        invitation.setStatus(status);
        invitation.setUpdateTime(LocalDateTime.now(clock));
        invitationMapper.updateById(invitation);
        auditService.recordSuccess(UcenterPermissionEvent.INVITATION_REJECTED,
                invitation.getSpaceId(), null,
                Set.of("status:PENDING"), Set.of("status:REJECTED"));
    }

    private UcenterInvitationEntity requirePendingInvitation(Long invitationId, Long actorId) {
        UcenterInvitationEntity invitation = invitationMapper.findByIdForUpdate(invitationId);
        if (invitation == null) throw notFound();
        if (!actorId.equals(invitation.getInviteeUserId())) {
            throw new UcenterDomainException(UcenterDomainException.Kind.FORBIDDEN, "该邀请不属于当前会员");
        }
        ensurePendingAndFresh(invitation);
        return invitation;
    }

    private void ensurePendingAndFresh(UcenterInvitationEntity invitation) {
        if (invitation.getStatus() != UcenterInvitationStatus.PENDING) {
            throw new UcenterDomainException(UcenterDomainException.Kind.INVALID_TRANSITION, "邀请已处理");
        }
        if (invitation.getExpiresAt() == null || !invitation.getExpiresAt().isAfter(LocalDateTime.now(clock))) {
            invitation.setStatus(UcenterInvitationStatus.EXPIRED);
            invitation.setUpdateTime(LocalDateTime.now(clock));
            invitationMapper.updateById(invitation);
            auditService.recordSuccess(UcenterPermissionEvent.INVITATION_EXPIRED,
                    invitation.getSpaceId(), null,
                    Set.of("status:PENDING"), Set.of("status:EXPIRED"));
            throw new UcenterInvitationExpiredException();
        }
    }

    private UcenterSpaceMemberEntity requireActiveMembership(Long userId) {
        UcenterSpaceMemberEntity member = memberMapper.findActiveByUserIdForUpdate(userId);
        if (member == null) {
            throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "请先初始化会员权限空间");
        }
        return member;
    }

    private static void requireOwner(UcenterSpaceMemberEntity member) {
        if (member.getRole() != UcenterRole.OWNER) {
            throw new UcenterDomainException(UcenterDomainException.Kind.FORBIDDEN, "仅空间所有者可执行此操作");
        }
    }

    private static String normalizeMobile(String input) {
        String normalized = input == null ? "" : input.replaceAll("[\\s-]", "");
        if (!normalized.matches("^\\+?\\d{6,20}$")) {
            throw new UcenterDomainException(UcenterDomainException.Kind.INVALID_INPUT, "请输入完整手机号");
        }
        return normalized;
    }

    private static Long parseUserId(String raw) {
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException error) {
            throw new UcenterDomainException(UcenterDomainException.Kind.NOT_FOUND, "会员身份无效");
        }
    }

    private static String lastFour(String mobile) {
        return mobile.substring(Math.max(0, mobile.length() - 4));
    }

    private static String randomDigest() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static UcenterDomainException notFound() {
        return new UcenterDomainException(UcenterDomainException.Kind.NOT_FOUND, "邀请不存在");
    }
}
