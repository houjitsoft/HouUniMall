package com.houjiang.permission.support;

import com.houjiang.permission.entity.PermissionMenu;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 角色/用户菜单权限常只勾选子节点，需补全父级目录才能在侧栏 buildMenuTree(pid=0) 中展示。
 * <p>
 * 祖先补全：一次加载 id/pid 后在内存完成；进程内短缓存，避免每次 role/edit 都全表扫。
 * @author Amos
 */
public final class MenuAncestorSupport {

    private static final Logger log = LoggerFactory.getLogger(MenuAncestorSupport.class);
    private static final long PID_CACHE_TTL_MS = 60_000L;

    private static final AtomicReference<PidSnapshot> PID_CACHE = new AtomicReference<>();

    private MenuAncestorSupport() {
    }

    public static List<Integer> expandMenuIdsWithAncestors(PermissionMenuMapper mapper, List<Integer> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }
        long t0 = System.nanoTime();
        PidSnapshot snap = loadPidSnapshot(mapper);
        List<Integer> expanded = expandMenuIdsWithAncestorsInMemory(menuIds, snap.pidOf, snap.disabled);
        long ms = (System.nanoTime() - t0) / 1_000_000L;
        if (ms > 200 || menuIds.size() > 500) {
            log.info("expandMenuIdsWithAncestors in={} out={} cost={}ms", menuIds.size(), expanded.size(), ms);
        }
        return expanded;
    }

    private static PidSnapshot loadPidSnapshot(PermissionMenuMapper mapper) {
        PidSnapshot cached = PID_CACHE.get();
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.loadedAtMs < PID_CACHE_TTL_MS) {
            return cached;
        }
        List<PermissionMenu> rows = mapper.selectAllIdPid();
        Map<Integer, Integer> pidOf = new HashMap<>(Math.max(16, rows.size() * 2));
        Set<Integer> disabled = new HashSet<>();
        for (PermissionMenu row : rows) {
            if (row == null || row.getId() == null) {
                continue;
            }
            pidOf.put(row.getId(), row.getPid() == null ? 0 : row.getPid());
            if (isDisabled(row)) {
                disabled.add(row.getId());
            }
        }
        PidSnapshot fresh = new PidSnapshot(pidOf, disabled, now);
        PID_CACHE.set(fresh);
        return fresh;
    }

    /** 菜单变更后可调用，避免短缓存脏读 */
    public static void invalidatePidCache() {
        PID_CACHE.set(null);
    }

    /** 纯内存补全，便于单测 */
    static List<Integer> expandMenuIdsWithAncestorsInMemory(List<Integer> menuIds,
                                                            Map<Integer, Integer> pidOf,
                                                            Set<Integer> disabled) {
        LinkedHashSet<Integer> expanded = new LinkedHashSet<>();
        Set<Integer> disabledIds = disabled != null ? disabled : Set.of();
        for (Integer id : menuIds) {
            if (id == null || id <= 0 || disabledIds.contains(id) || !pidOf.containsKey(id)) {
                continue;
            }
            expanded.add(id);
            Integer pid = pidOf.get(id);
            while (pid != null && pid > 0) {
                if (expanded.contains(pid)) {
                    break;
                }
                if (disabledIds.contains(pid) || !pidOf.containsKey(pid)) {
                    break;
                }
                expanded.add(pid);
                pid = pidOf.get(pid);
            }
        }
        return new ArrayList<>(expanded);
    }

    public static List<PermissionMenu> expandMenusWithAncestors(PermissionMenuMapper mapper,
                                                                 List<PermissionMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }
        // selectAllIdPid 仅有 id/pid/is_disable，绝不能用它覆盖用户菜单的完整行（否则 name/type/paths 全空，侧栏空白）
        List<PermissionMenu> rows = mapper.selectAllIdPid();
        Map<Integer, Integer> pidOf = new HashMap<>(Math.max(16, rows.size() * 2));
        Set<Integer> disabled = new HashSet<>();
        for (PermissionMenu row : rows) {
            if (row == null || row.getId() == null) {
                continue;
            }
            pidOf.put(row.getId(), row.getPid() == null ? 0 : row.getPid());
            if (isDisabled(row)) {
                disabled.add(row.getId());
            }
        }

        LinkedHashMap<Integer, PermissionMenu> result = new LinkedHashMap<>();
        LinkedHashSet<Integer> missingAncestorIds = new LinkedHashSet<>();
        for (PermissionMenu m : menus) {
            if (m == null || m.getId() == null || disabled.contains(m.getId())) {
                continue;
            }
            result.put(m.getId(), m);
            Integer pid = m.getPid() != null ? m.getPid() : pidOf.get(m.getId());
            while (pid != null && pid > 0) {
                if (result.containsKey(pid) || missingAncestorIds.contains(pid)) {
                    break;
                }
                if (disabled.contains(pid) || !pidOf.containsKey(pid)) {
                    break;
                }
                missingAncestorIds.add(pid);
                pid = pidOf.get(pid);
            }
        }

        if (!missingAncestorIds.isEmpty()) {
            List<PermissionMenu> ancestors = mapper.selectBatchIds(missingAncestorIds);
            if (ancestors != null) {
                for (PermissionMenu parent : ancestors) {
                    if (parent == null || parent.getId() == null || disabled.contains(parent.getId())) {
                        continue;
                    }
                    result.putIfAbsent(parent.getId(), parent);
                }
            }
        }
        return new ArrayList<>(result.values());
    }

    private static boolean isDisabled(PermissionMenu menu) {
        return menu.getIsDisable() != null && menu.getIsDisable() != 0;
    }

    private record PidSnapshot(Map<Integer, Integer> pidOf, Set<Integer> disabled, long loadedAtMs) {
    }
}
