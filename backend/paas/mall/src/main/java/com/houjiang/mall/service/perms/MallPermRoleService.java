package com.houjiang.mall.service.perms;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.dto.auth.MallPageResult;
import com.houjiang.mall.entity.auth.MallAdminRole;
import com.houjiang.mall.entity.auth.MallSystemRole;
import com.houjiang.mall.entity.auth.MallSystemRoleMenu;
import com.houjiang.mall.mapper.auth.MallAdminRoleMapper;
import com.houjiang.mall.mapper.auth.MallSystemRoleMapper;
import com.houjiang.mall.mapper.auth.MallSystemRoleMenuMapper;
import com.houjiang.mall.util.MallPermFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Service
public class MallPermRoleService {

    private final MallSystemRoleMapper mallSystemRoleMapper;
    private final MallSystemRoleMenuMapper mallSystemRoleMenuMapper;
    private final MallAdminRoleMapper mallAdminRoleMapper;

    public ResponseResult all() {
        QueryWrapper<MallSystemRole> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.orderByDesc("sort", "id");
        List<MallSystemRole> roles = mallSystemRoleMapper.selectList(wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (MallSystemRole role : roles) {
            list.add(toRoleMap(role, false));
        }
        return ResponseResult.success(list);
    }

    public ResponseResult lists(Map<String, Object> params) {
        int pageNo = MallPermFormatUtil.pageNo(params);
        int pageSize = MallPermFormatUtil.pageSize(params);
        QueryWrapper<MallSystemRole> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.orderByDesc("sort", "id");
        Page<MallSystemRole> page = mallSystemRoleMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (MallSystemRole role : page.getRecords()) {
            list.add(toRoleMap(role, true));
        }
        return ResponseResult.success(MallPageResult.of(page, list));
    }

    public ResponseResult detail(Integer id) {
        MallSystemRole role = findRole(id);
        if (role == null) {
            return ResponseResult.fail(404, "角色已不存在");
        }
        Map<String, Object> map = toRoleMap(role, true);
        map.put("menu_id", selectMenuIds(id));
        return ResponseResult.success(map);
    }

    public ResponseResult add(Map<String, Object> params) {
        String name = strVal(params.get("name"));
        if (!StringUtils.hasText(name)) {
            return ResponseResult.fail(400, "角色名称不能为空");
        }
        if (existsRoleName(name, null)) {
            return ResponseResult.fail(400, "角色名称已存在");
        }
        int now = currentTs();
        MallSystemRole role = new MallSystemRole();
        role.setName(name.trim());
        role.setDesc(strVal(params.get("desc")));
        role.setSort(intVal(params.get("sort"), 0));
        role.setCreateTime(now);
        role.setUpdateTime(now);
        mallSystemRoleMapper.insert(role);
        saveRoleMenus(role.getId(), params.get("menu_id"));
        return ResponseResult.success();
    }

    @Transactional
    public ResponseResult edit(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "角色ID不能为空");
        }
        MallSystemRole role = findRole(id);
        if (role == null) {
            return ResponseResult.fail(404, "角色已不存在");
        }
        String name = strVal(params.get("name"));
        if (StringUtils.hasText(name)) {
            if (existsRoleName(name.trim(), id)) {
                return ResponseResult.fail(400, "角色名称已存在");
            }
            role.setName(name.trim());
        }
        if (params.containsKey("desc")) {
            role.setDesc(strVal(params.get("desc")));
        }
        if (params.containsKey("sort")) {
            role.setSort(intVal(params.get("sort"), role.getSort() == null ? 0 : role.getSort()));
        }
        role.setUpdateTime(currentTs());
        mallSystemRoleMapper.updateById(role);

        if (params.containsKey("menu_id")) {
            mallSystemRoleMenuMapper.delete(new QueryWrapper<MallSystemRoleMenu>().eq("role_id", id));
            saveRoleMenus(id, params.get("menu_id"));
        }
        return ResponseResult.success();
    }

    @Transactional
    public ResponseResult delete(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "角色ID不能为空");
        }
        MallSystemRole role = findRole(id);
        if (role == null) {
            return ResponseResult.fail(404, "角色已不存在");
        }
        Long adminCount = mallAdminRoleMapper.selectCount(new QueryWrapper<MallAdminRole>().eq("role_id", id));
        if (adminCount != null && adminCount > 0) {
            return ResponseResult.fail(400, "角色已被管理员使用，请先移除");
        }
        mallSystemRoleMapper.deleteById(id);
        mallSystemRoleMenuMapper.delete(new QueryWrapper<MallSystemRoleMenu>().eq("role_id", id));
        return ResponseResult.success();
    }

    private MallSystemRole findRole(Integer id) {
        QueryWrapper<MallSystemRole> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.eq("id", id).last("limit 1");
        return mallSystemRoleMapper.selectOne(wrapper);
    }

    private boolean existsRoleName(String name, Integer excludeId) {
        QueryWrapper<MallSystemRole> wrapper = notDeletedWrapper(new QueryWrapper<>());
        wrapper.eq("name", name);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        wrapper.last("limit 1");
        return mallSystemRoleMapper.selectOne(wrapper) != null;
    }

    private Map<String, Object> toRoleMap(MallSystemRole role, boolean withMenuIds) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", role.getId());
        map.put("name", role.getName());
        map.put("desc", role.getDesc() == null ? "" : role.getDesc());
        map.put("sort", role.getSort());
        map.put("num", countAdminByRole(role.getId()));
        map.put("create_time", MallPermFormatUtil.formatTime(role.getCreateTime()));
        map.put("update_time", MallPermFormatUtil.formatTime(role.getUpdateTime()));
        if (withMenuIds) {
            map.put("menu_id", selectMenuIds(role.getId()));
        } else {
            map.put("menu_id", Collections.emptyList());
        }
        return map;
    }

    private int countAdminByRole(Integer roleId) {
        Long count = mallAdminRoleMapper.selectCount(new QueryWrapper<MallAdminRole>().eq("role_id", roleId));
        return count == null ? 0 : count.intValue();
    }

    private List<Integer> selectMenuIds(Integer roleId) {
        List<MallSystemRoleMenu> refs = mallSystemRoleMenuMapper.selectList(
                new QueryWrapper<MallSystemRoleMenu>().eq("role_id", roleId));
        if (refs == null || refs.isEmpty()) {
            return Collections.emptyList();
        }
        return refs.stream().map(MallSystemRoleMenu::getMenuId).collect(Collectors.toList());
    }

    private void saveRoleMenus(Integer roleId, Object menuIdsRaw) {
        List<Integer> menuIds = MallPermFormatUtil.intList(menuIdsRaw);
        for (Integer menuId : menuIds) {
            MallSystemRoleMenu ref = new MallSystemRoleMenu();
            ref.setRoleId(roleId);
            ref.setMenuId(menuId);
            mallSystemRoleMenuMapper.insert(ref);
        }
    }

    private QueryWrapper<MallSystemRole> notDeletedWrapper(QueryWrapper<MallSystemRole> wrapper) {
        wrapper.and(w -> w.isNull("delete_time").or().eq("delete_time", 0));
        return wrapper;
    }

    private int currentTs() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private String strVal(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Integer intVal(Object value) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Integer intVal(Object value, int defaultValue) {
        Integer parsed = intVal(value);
        return parsed == null ? defaultValue : parsed;
    }
}
