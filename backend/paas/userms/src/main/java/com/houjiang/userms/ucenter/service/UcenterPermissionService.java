package com.houjiang.userms.ucenter.service;

import com.houjiang.userms.ucenter.mapper.UcenterInvitationMapper;
import com.houjiang.userms.ucenter.mapper.UcenterMemberMenuMapper;
import com.houjiang.userms.ucenter.mapper.UcenterMenuMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMemberMapper;
import com.houjiang.userms.ucenter.model.UcenterMemberMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterMenuEntity;
import com.houjiang.userms.ucenter.model.UcenterRole;
import com.houjiang.userms.ucenter.model.UcenterSpaceMemberEntity;
import com.houjiang.userms.ucenter.vo.UcenterMenuVO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UcenterPermissionService {
    private final UcenterMenuMapper menuMapper;
    private final UcenterMemberMenuMapper memberMenuMapper;
    private final UcenterInvitationMapper invitationMapper;
    private final UcenterIdentityService identityService;
    private final UcenterSpaceMemberMapper memberMapper;
    private final UcenterSpaceMapper spaceMapper;
    private final UcenterAuditService auditService;

    public UcenterPermissionService(UcenterMenuMapper menuMapper,
                                    UcenterMemberMenuMapper memberMenuMapper,
                                    UcenterInvitationMapper invitationMapper) {
        this(menuMapper, memberMenuMapper, invitationMapper, null, null, null, null);
    }

    @Autowired
    public UcenterPermissionService(UcenterMenuMapper menuMapper,
                                    UcenterMemberMenuMapper memberMenuMapper,
                                    UcenterInvitationMapper invitationMapper,
                                    UcenterIdentityService identityService,
                                    UcenterSpaceMemberMapper memberMapper,
                                    UcenterSpaceMapper spaceMapper,
                                    UcenterAuditService auditService) {
        this.menuMapper = menuMapper;
        this.memberMenuMapper = memberMenuMapper;
        this.invitationMapper = invitationMapper;
        this.identityService = identityService;
        this.memberMapper = memberMapper;
        this.spaceMapper = spaceMapper;
        this.auditService = auditService;
    }

    public List<UcenterMenuVO> effectiveMenuTree(UcenterSpaceMemberEntity member) {
        List<UcenterMenuEntity> catalog = menuMapper.findEnabledVisible();
        Set<Long> grantedIds = member.getRole() == UcenterRole.OWNER
                ? Set.of()
                : memberMenuMapper.findGrantedMenuIds(member.getId());
        if (grantedIds == null) grantedIds = Set.of();

        Set<String> includedCodes = new HashSet<>();
        Map<String, UcenterMenuEntity> byCode = new HashMap<>();
        for (UcenterMenuEntity item : catalog) {
            byCode.put(item.getCode(), item);
            if (isAllowedLeaf(item, member.getRole(), grantedIds)) includedCodes.add(item.getCode());
        }
        for (String code : List.copyOf(includedCodes)) {
            UcenterMenuEntity item = byCode.get(code);
            while (item != null && item.getParentCode() != null) {
                includedCodes.add(item.getParentCode());
                item = byCode.get(item.getParentCode());
            }
        }

        Map<String, List<UcenterMenuEntity>> children = new LinkedHashMap<>();
        List<UcenterMenuEntity> roots = new ArrayList<>();
        for (UcenterMenuEntity item : catalog) {
            if (!includedCodes.contains(item.getCode())) continue;
            if (item.getParentCode() == null) roots.add(item);
            else children.computeIfAbsent(item.getParentCode(), ignored -> new ArrayList<>()).add(item);
        }
        Comparator<UcenterMenuEntity> order = Comparator
                .comparing(UcenterMenuEntity::getSort, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UcenterMenuEntity::getId);
        roots.sort(order);
        children.values().forEach(items -> items.sort(order));
        return roots.stream().map(root -> toTree(root, children)).toList();
    }

    public boolean mayAccess(UcenterSpaceMemberEntity member, String code) {
        if (code == null || code.isBlank()) return false;
        return flattenCodes(effectiveMenuTree(member)).contains(code);
    }

    public boolean mayCurrentUserAccess(String code) {
        UcenterIdentityService.CurrentUser actor = identityService.requireCurrentUser();
        UcenterSpaceMemberEntity membership = memberMapper.findActiveByUserId(actor.userId());
        if (membership == null) {
            throw new UcenterDomainException(UcenterDomainException.Kind.CONFLICT,
                    "请先初始化会员权限空间");
        }
        if (code != null && code.startsWith("ucenter:enterprise:") && !actor.business()) return false;
        return mayAccess(membership, code);
    }

    public long pendingInvitationCount(Long userId) {
        return invitationMapper.countPendingByInvitee(userId);
    }

    public List<UcenterMenuEntity> requireConfigurableMenus(Set<String> codes) {
        if (codes == null || codes.isEmpty()) return List.of();
        List<UcenterMenuEntity> result = menuMapper.findEnabledByCodes(codes);
        Set<String> resolved = new HashSet<>();
        for (UcenterMenuEntity item : result) {
            boolean configurable = item.getParentCode() != null
                    && !Boolean.TRUE.equals(item.getMandatory())
                    && !Boolean.TRUE.equals(item.getOwnerOnly());
            if (!configurable) {
                throw new UcenterDomainException(UcenterDomainException.Kind.INVALID_INPUT,
                        "菜单不可分配：" + item.getCode());
            }
            resolved.add(item.getCode());
        }
        if (!resolved.equals(codes)) {
            throw new UcenterDomainException(UcenterDomainException.Kind.INVALID_INPUT, "包含未知或已停用菜单");
        }
        return result;
    }

    public List<UcenterMenuEntity> configurableCatalog() {
        return menuMapper.findEnabledVisible().stream()
                .filter(item -> item.getParentCode() != null)
                .filter(item -> !Boolean.TRUE.equals(item.getMandatory()))
                .filter(item -> !Boolean.TRUE.equals(item.getOwnerOnly()))
                .toList();
    }

    @Transactional
    public void replaceMemberMenus(Long targetMemberId, Set<String> codes) {
        Long actorId = identityService.requireUserId();
        UcenterSpaceMemberEntity actor = memberMapper.findActiveByUserIdForUpdate(actorId);
        if (actor == null || actor.getRole() != UcenterRole.OWNER) {
            throw new UcenterDomainException(UcenterDomainException.Kind.FORBIDDEN,
                    "仅空间所有者可分配菜单");
        }
        UcenterSpaceMemberEntity target = memberMapper.findActiveByIdForUpdate(targetMemberId);
        if (target == null || !actor.getSpaceId().equals(target.getSpaceId())) {
            throw new UcenterDomainException(UcenterDomainException.Kind.NOT_FOUND, "空间成员不存在");
        }
        if (target.getRole() == UcenterRole.OWNER) {
            throw new UcenterDomainException(UcenterDomainException.Kind.INVALID_INPUT, "所有者权限不可限制");
        }
        List<UcenterMenuEntity> desired = requireConfigurableMenus(codes);
        Set<String> before = memberMenuMapper.findGrantedMenuCodes(targetMemberId);
        if (before == null) before = Set.of();
        memberMenuMapper.deleteByMemberId(targetMemberId);
        for (UcenterMenuEntity menu : desired) {
            UcenterMemberMenuEntity grant = new UcenterMemberMenuEntity();
            grant.setMemberId(targetMemberId);
            grant.setMenuId(menu.getId());
            grant.setGrantedBy(actorId);
            grant.setCreateTime(java.time.LocalDateTime.now());
            memberMenuMapper.insert(grant);
        }
        spaceMapper.incrementVersion(actor.getSpaceId());
        auditService.recordSuccess(com.houjiang.userms.ucenter.model.UcenterPermissionEvent.MENUS_REPLACED,
                actor.getSpaceId(), targetMemberId, before, Set.copyOf(codes));
    }

    private static boolean isAllowedLeaf(UcenterMenuEntity item, UcenterRole role, Set<Long> grantedIds) {
        if (item.getParentCode() == null) return false;
        if (role == UcenterRole.OWNER) return true;
        if (Boolean.TRUE.equals(item.getOwnerOnly())) return false;
        return Boolean.TRUE.equals(item.getMandatory()) || grantedIds.contains(item.getId());
    }

    private static UcenterMenuVO toTree(UcenterMenuEntity item,
                                        Map<String, List<UcenterMenuEntity>> children) {
        List<UcenterMenuVO> nested = children.getOrDefault(item.getCode(), List.of()).stream()
                .map(child -> toTree(child, children))
                .toList();
        return new UcenterMenuVO(
                item.getCode(), item.getNameKey(), item.getRoutePath(), item.getIcon(),
                Boolean.TRUE.equals(item.getMandatory()), Boolean.TRUE.equals(item.getOwnerOnly()), nested);
    }

    private static Set<String> flattenCodes(List<UcenterMenuVO> roots) {
        Set<String> codes = new HashSet<>();
        for (UcenterMenuVO root : roots) {
            codes.add(root.code());
            codes.addAll(flattenCodes(root.children()));
        }
        return codes;
    }
}
