package com.houjiang.userms.ucenter.service;

import com.houjiang.userms.ucenter.mapper.UcenterSpaceMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMemberMapper;
import com.houjiang.userms.ucenter.mapper.UcenterMemberMenuMapper;
import com.houjiang.userms.ucenter.model.UcenterMemberMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterPermissionEvent;
import com.houjiang.userms.ucenter.vo.UcenterMemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.userms.ucenter.model.UcenterRole;
import com.houjiang.userms.ucenter.model.UcenterSpaceEntity;
import com.houjiang.userms.ucenter.model.UcenterSpaceMemberEntity;
import com.houjiang.userms.ucenter.vo.UcenterBootstrapVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class UcenterSpaceService {
    private final UcenterIdentityService identityService;
    private final UcenterSpaceMapper spaceMapper;
    private final UcenterSpaceMemberMapper memberMapper;
    private final UcenterPermissionService permissionService;
    private final UcenterMemberMenuMapper memberMenuMapper;
    private final UcenterAuditService auditService;

    public UcenterSpaceService(UcenterIdentityService identityService,
                               UcenterSpaceMapper spaceMapper,
                               UcenterSpaceMemberMapper memberMapper,
                               UcenterPermissionService permissionService) {
        this(identityService, spaceMapper, memberMapper, permissionService, null, null);
    }

    @Autowired
    public UcenterSpaceService(UcenterIdentityService identityService,
                               UcenterSpaceMapper spaceMapper,
                               UcenterSpaceMemberMapper memberMapper,
                               UcenterPermissionService permissionService,
                               UcenterMemberMenuMapper memberMenuMapper,
                               UcenterAuditService auditService) {
        this.identityService = identityService;
        this.spaceMapper = spaceMapper;
        this.memberMapper = memberMapper;
        this.permissionService = permissionService;
        this.memberMenuMapper = memberMenuMapper;
        this.auditService = auditService;
    }

    @Transactional
    public UcenterBootstrapVO bootstrapForCurrentUser() {
        UcenterIdentityService.CurrentUser currentUser = identityService.requireCurrentUser();
        UcenterSpaceMemberEntity membership = memberMapper.findActiveByUserIdForUpdate(currentUser.userId());
        UcenterSpaceEntity space;
        if (membership == null) {
            try {
                space = createPersonalSpace(currentUser);
                membership = createOwnerMembership(space.getId(), currentUser.userId());
            } catch (DuplicateKeyException race) {
                membership = memberMapper.findActiveByUserId(currentUser.userId());
                if (membership == null) throw race;
                space = spaceMapper.selectById(membership.getSpaceId());
            }
        } else {
            space = spaceMapper.selectById(membership.getSpaceId());
        }
        if (space == null || !Integer.valueOf(1).equals(space.getStatus())) {
            throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "会员权限空间不可用");
        }
        return toBootstrap(currentUser, space, membership);
    }

    public List<UcenterMemberVO> membersForOwner() {
        Long actorId = identityService.requireUserId();
        UcenterSpaceMemberEntity actor = memberMapper.findActiveByUserId(actorId);
        requireOwner(actor);
        return memberMapper.findActiveBySpaceId(actor.getSpaceId()).stream()
                .map(member -> new UcenterMemberVO(
                        member.getId(), member.getUserId(), member.getRole(), member.getJoinTime(),
                        member.getRole() == UcenterRole.OWNER
                                ? permissionService.configurableCatalog().stream()
                                    .map(UcenterMenuEntity::getCode).collect(java.util.stream.Collectors.toUnmodifiableSet())
                                : safeCodes(memberMenuMapper.findGrantedMenuCodes(member.getId()))))
                .toList();
    }

    @Transactional
    public void leaveCurrentSpace() {
        UcenterIdentityService.CurrentUser user = identityService.requireCurrentUser();
        UcenterSpaceMemberEntity current = memberMapper.findActiveByUserIdForUpdate(user.userId());
        if (current == null) {
            throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "当前会员没有有效权限空间");
        }
        if (current.getRole() == UcenterRole.OWNER) {
            if (memberMapper.countOtherActiveMembers(current.getSpaceId(), user.userId()) > 0) {
                throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT,
                        "请先转让所有权或移除其他成员");
            }
            if (spaceMapper.closeActiveSpace(current.getSpaceId(), user.userId()) != 1) {
                throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "空间状态已变化，请重试");
            }
        }
        if (memberMapper.markLeft(current.getId(), user.userId()) != 1) {
            throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "成员状态已变化，请重试");
        }
        memberMenuMapper.deleteByMemberId(current.getId());
        spaceMapper.incrementVersion(current.getSpaceId());
        UcenterSpaceEntity personal = createPersonalSpace(user);
        createOwnerMembership(personal.getId(), user.userId());
        auditService.recordSuccess(UcenterPermissionEvent.MEMBER_LEFT, current.getSpaceId(),
                current.getId(), Set.of(), Set.of());
    }

    @Transactional
    public void removeMember(Long targetMemberId) {
        UcenterIdentityService.CurrentUser actorUser = identityService.requireCurrentUser();
        UcenterSpaceMemberEntity actor = memberMapper.findActiveByUserIdForUpdate(actorUser.userId());
        requireOwner(actor);
        UcenterSpaceMemberEntity target = requireSameSpaceMember(actor, targetMemberId);
        if (target.getRole() == UcenterRole.OWNER) {
            throw new UcenterDomainException(UcenterDomainException.Kind.INVALID_INPUT, "不能移除空间所有者");
        }
        Set<String> before = safeCodes(memberMenuMapper.findGrantedMenuCodes(targetMemberId));
        memberMenuMapper.deleteByMemberId(targetMemberId);
        if (memberMapper.markLeft(targetMemberId, target.getUserId()) != 1) {
            throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "成员状态已变化，请重试");
        }
        spaceMapper.incrementVersion(actor.getSpaceId());
        UcenterSpaceEntity personal = createPersonalSpaceFor(target.getUserId(), "会员" + target.getUserId());
        createOwnerMembership(personal.getId(), target.getUserId());
        auditService.recordSuccess(UcenterPermissionEvent.MEMBER_REMOVED, actor.getSpaceId(),
                targetMemberId, before, Set.of());
    }

    @Transactional
    public void transferOwnership(Long targetMemberId) {
        Long actorId = identityService.requireUserId();
        UcenterSpaceMemberEntity actor = memberMapper.findActiveByUserIdForUpdate(actorId);
        requireOwner(actor);
        UcenterSpaceMemberEntity target = requireSameSpaceMember(actor, targetMemberId);
        if (target.getRole() != UcenterRole.MEMBER) {
            throw new UcenterDomainException(UcenterDomainException.Kind.INVALID_INPUT, "目标必须是普通成员");
        }
        if (memberMapper.updateRole(actor.getId(), UcenterRole.MEMBER.name()) != 1
                || memberMapper.updateRole(target.getId(), UcenterRole.OWNER.name()) != 1
                || spaceMapper.transferOwner(actor.getSpaceId(), actorId, target.getUserId()) != 1) {
            throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT, "所有权状态已变化，请重试");
        }
        memberMenuMapper.deleteByMemberId(actor.getId());
        LocalDateTime now = LocalDateTime.now();
        for (UcenterMenuEntity menu : permissionService.configurableCatalog()) {
            UcenterMemberMenuEntity grant = new UcenterMemberMenuEntity();
            grant.setMemberId(actor.getId());
            grant.setMenuId(menu.getId());
            grant.setGrantedBy(target.getUserId());
            grant.setCreateTime(now);
            memberMenuMapper.insert(grant);
        }
        auditService.recordSuccess(UcenterPermissionEvent.OWNERSHIP_TRANSFERRED, actor.getSpaceId(),
                targetMemberId,
                Set.of("member:" + actor.getId() + ":OWNER",
                        "member:" + target.getId() + ":MEMBER"),
                Set.of("member:" + actor.getId() + ":MEMBER",
                        "member:" + target.getId() + ":OWNER"));
    }

    private UcenterSpaceEntity createPersonalSpace(UcenterIdentityService.CurrentUser currentUser) {
        return createPersonalSpaceFor(currentUser.userId(), currentUser.displayName());
    }

    private UcenterSpaceEntity createPersonalSpaceFor(Long userId, String displayName) {
        LocalDateTime now = LocalDateTime.now();
        UcenterSpaceEntity space = new UcenterSpaceEntity();
        space.setOwnerUserId(userId);
        space.setName(displayName + "的个人空间");
        space.setStatus(1);
        space.setVersion(1L);
        space.setCreateTime(now);
        space.setUpdateTime(now);
        spaceMapper.insert(space);
        return space;
    }

    private UcenterSpaceMemberEntity createOwnerMembership(Long spaceId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        UcenterSpaceMemberEntity member = new UcenterSpaceMemberEntity();
        member.setSpaceId(spaceId);
        member.setUserId(userId);
        member.setRole(UcenterRole.OWNER);
        member.setStatus("ACTIVE");
        member.setJoinedBy(userId);
        member.setJoinTime(now);
        member.setCreateTime(now);
        member.setUpdateTime(now);
        memberMapper.insert(member);
        return member;
    }

    private UcenterBootstrapVO toBootstrap(UcenterIdentityService.CurrentUser user,
                                           UcenterSpaceEntity space,
                                           UcenterSpaceMemberEntity membership) {
        long version = space.getVersion() == null ? 1L : space.getVersion();
        return new UcenterBootstrapVO(
                new UcenterBootstrapVO.ProfileSummary(
                        user.userId(), user.displayName(), user.maskedMobile(), user.avatar(), user.business()),
                new UcenterBootstrapVO.SpaceSummary(
                        space.getId(), space.getName(), space.getOwnerUserId(), version),
                membership.getRole(),
                permissionService.effectiveMenuTree(membership),
                permissionService.pendingInvitationCount(user.userId()),
                version
        );
    }

    private UcenterSpaceMemberEntity requireSameSpaceMember(UcenterSpaceMemberEntity actor, Long memberId) {
        UcenterSpaceMemberEntity target = memberMapper.findActiveByIdForUpdate(memberId);
        if (target == null || !actor.getSpaceId().equals(target.getSpaceId())) {
            throw new UcenterDomainException(UcenterDomainException.Kind.NOT_FOUND, "空间成员不存在");
        }
        return target;
    }

    private static void requireOwner(UcenterSpaceMemberEntity member) {
        if (member == null || member.getRole() != UcenterRole.OWNER) {
            throw new UcenterDomainException(UcenterDomainException.Kind.FORBIDDEN, "仅空间所有者可执行此操作");
        }
    }

    private static Set<String> safeCodes(Set<String> codes) {
        return codes == null ? Set.of() : Set.copyOf(codes);
    }
}
