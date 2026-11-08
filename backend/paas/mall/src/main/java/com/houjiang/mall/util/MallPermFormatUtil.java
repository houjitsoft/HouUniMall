package com.houjiang.mall.util;

import com.houjiang.mall.dto.auth.MallAuthMenuVo;
import com.houjiang.mall.entity.auth.MallSystemMenu;
import org.springframework.beans.BeanUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Mall 权限模块格式化工具（对齐 like-admin 前端字段）
 * @author Amos
 */
public final class MallPermFormatUtil {

    private MallPermFormatUtil() {
    }

    public static String formatTime(Integer ts) {
        if (ts == null || ts <= 0) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ts.longValue() * 1000L));
    }

    public static MallAuthMenuVo toMenuVo(MallSystemMenu menu) {
        MallAuthMenuVo vo = new MallAuthMenuVo();
        BeanUtils.copyProperties(menu, vo);
        if (menu.getPaths() != null && menu.getPaths().startsWith("/")) {
            vo.setPaths(menu.getPaths().substring(1));
        }
        return vo;
    }

    public static Map<String, Object> toMenuMap(MallSystemMenu menu) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", menu.getId());
        map.put("pid", menu.getPid());
        map.put("type", normalizeType(menu.getType()));
        map.put("name", menu.getName());
        map.put("icon", menu.getIcon());
        map.put("sort", menu.getSort());
        map.put("perms", menu.getPerms());
        map.put("paths", normalizePath(menu.getPaths()));
        map.put("component", menu.getComponent());
        map.put("selected", menu.getSelected());
        map.put("params", menu.getParams());
        map.put("is_cache", menu.getIsCache());
        map.put("is_show", menu.getIsShow());
        map.put("is_disable", menu.getIsDisable());
        map.put("create_time", formatTime(menu.getCreateTime()));
        map.put("update_time", formatTime(menu.getUpdateTime()));
        return map;
    }

    public static List<Map<String, Object>> buildMenuTreeMaps(List<MallSystemMenu> menus) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<Integer, Map<String, Object>> index = new LinkedHashMap<>();
        for (MallSystemMenu menu : menus) {
            Map<String, Object> node = toMenuMap(menu);
            node.put("children", new ArrayList<Map<String, Object>>());
            index.put(menu.getId(), node);
            nodes.add(node);
        }
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map<String, Object> node : nodes) {
            Integer pid = (Integer) node.get("pid");
            if (pid == null || pid == 0 || !index.containsKey(pid)) {
                tree.add(node);
                continue;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) index.get(pid).get("children");
            children.add(node);
        }
        for (Map<String, Object> node : nodes) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children.isEmpty()) {
                node.remove("children");
            }
        }
        return tree;
    }

    public static int pageNo(Map<String, Object> params) {
        Object page = params.get("page_no");
        if (page == null) {
            page = params.get("page");
        }
        return page == null ? 1 : Integer.parseInt(String.valueOf(page));
    }

    public static int pageSize(Map<String, Object> params) {
        Object size = params.get("page_size");
        if (size == null) {
            size = params.get("size");
        }
        return size == null ? 15 : Integer.parseInt(String.valueOf(size));
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> buildTree(List<Map<String, Object>> flat) {
        if (flat == null || flat.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Object, Map<String, Object>> index = new LinkedHashMap<>();
        for (Map<String, Object> node : flat) {
            Map<String, Object> copy = new LinkedHashMap<>(node);
            copy.put("children", new ArrayList<Map<String, Object>>());
            index.put(copy.get("id"), copy);
        }
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map<String, Object> node : index.values()) {
            Object pid = node.get("pid");
            boolean isRoot = pid == null || "0".equals(String.valueOf(pid)) || Integer.parseInt(String.valueOf(pid)) == 0;
            if (isRoot || !index.containsKey(pid instanceof Integer ? pid : Integer.parseInt(String.valueOf(pid)))) {
                tree.add(node);
                continue;
            }
            int parentId = pid instanceof Integer ? (Integer) pid : Integer.parseInt(String.valueOf(pid));
            List<Map<String, Object>> children = (List<Map<String, Object>>) index.get(parentId).get("children");
            children.add(node);
        }
        for (Map<String, Object> node : index.values()) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children.isEmpty()) {
                node.remove("children");
            }
        }
        return tree;
    }

    public static List<Integer> intList(Object raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        if (raw instanceof List) {
            List<Integer> result = new ArrayList<>();
            for (Object item : (List<?>) raw) {
                if (item != null && String.valueOf(item).length() > 0) {
                    result.add(Integer.parseInt(String.valueOf(item)));
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private static String normalizePath(String paths) {
        if (paths == null || !paths.startsWith("/")) {
            return paths;
        }
        return paths.substring(1);
    }

    private static String normalizeType(String type) {
        if ("D".equals(type)) {
            return "M";
        }
        if ("B".equals(type)) {
            return "A";
        }
        return type;
    }
}
