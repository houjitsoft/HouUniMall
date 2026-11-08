package com.houjiang.mall.service.perms;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.auth.MallSystemMenu;
import com.houjiang.mall.entity.auth.MallSystemRoleMenu;
import com.houjiang.mall.mapper.auth.MallSystemMenuMapper;
import com.houjiang.mall.mapper.auth.MallSystemRoleMenuMapper;
import com.houjiang.mall.util.MallPermFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Service
public class MallPermMenuService {

    private final MallSystemMenuMapper mallSystemMenuMapper;
    private final MallSystemRoleMenuMapper mallSystemRoleMenuMapper;

    public ResponseResult list() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lists", allTree());
        return ResponseResult.success(result);
    }

    public ResponseResult all() {
        return ResponseResult.success(allTree());
    }

    public ResponseResult tree() {
        return ResponseResult.success(allTree());
    }

    public ResponseResult detail(Integer id) {
        MallSystemMenu menu = mallSystemMenuMapper.selectById(id);
        if (menu == null) {
            return ResponseResult.fail(404, "菜单已不存在");
        }
        return ResponseResult.success(MallPermFormatUtil.toMenuMap(menu));
    }

    public ResponseResult add(Map<String, Object> params) {
        MallSystemMenu menu = buildMenuFromParams(params, null);
        int now = currentTs();
        menu.setCreateTime(now);
        menu.setUpdateTime(now);
        mallSystemMenuMapper.insert(menu);
        return ResponseResult.success();
    }

    public ResponseResult edit(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "菜单ID不能为空");
        }
        MallSystemMenu existing = mallSystemMenuMapper.selectById(id);
        if (existing == null) {
            return ResponseResult.fail(404, "菜单已不存在");
        }
        MallSystemMenu menu = buildMenuFromParams(params, existing);
        menu.setId(id);
        menu.setUpdateTime(currentTs());
        mallSystemMenuMapper.updateById(menu);
        return ResponseResult.success();
    }

    public ResponseResult delete(Map<String, Object> params) {
        Integer id = intVal(params.get("id"));
        if (id == null) {
            return ResponseResult.fail(400, "菜单ID不能为空");
        }
        MallSystemMenu existing = mallSystemMenuMapper.selectById(id);
        if (existing == null) {
            return ResponseResult.fail(404, "菜单已不存在");
        }
        Long childCount = mallSystemMenuMapper.selectCount(new QueryWrapper<MallSystemMenu>().eq("pid", id));
        if (childCount != null && childCount > 0) {
            return ResponseResult.fail(400, "请先删除子菜单");
        }
        Long roleRefCount = mallSystemRoleMenuMapper.selectCount(new QueryWrapper<MallSystemRoleMenu>().eq("menu_id", id));
        if (roleRefCount != null && roleRefCount > 0) {
            return ResponseResult.fail(400, "菜单已被角色使用，无法删除");
        }
        mallSystemMenuMapper.deleteById(id);
        return ResponseResult.success();
    }

    private List<Map<String, Object>> allTree() {
        QueryWrapper<MallSystemMenu> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("sort").orderByAsc("id");
        List<MallSystemMenu> menus = mallSystemMenuMapper.selectList(wrapper);
        return MallPermFormatUtil.buildMenuTreeMaps(menus);
    }

    private MallSystemMenu buildMenuFromParams(Map<String, Object> params, MallSystemMenu base) {
        MallSystemMenu menu = base == null ? new MallSystemMenu() : base;
        if (params.containsKey("pid")) {
            menu.setPid(intVal(params.get("pid"), 0));
        }
        if (params.containsKey("type")) {
            menu.setType(String.valueOf(params.get("type")));
        }
        if (params.containsKey("name")) {
            menu.setName(String.valueOf(params.get("name")));
        }
        if (params.containsKey("icon")) {
            menu.setIcon(String.valueOf(params.get("icon")));
        }
        if (params.containsKey("sort")) {
            menu.setSort(intVal(params.get("sort"), 0));
        }
        if (params.containsKey("perms")) {
            menu.setPerms(String.valueOf(params.get("perms")));
        }
        if (params.containsKey("paths")) {
            menu.setPaths(String.valueOf(params.get("paths")));
        }
        if (params.containsKey("component")) {
            menu.setComponent(String.valueOf(params.get("component")));
        }
        if (params.containsKey("selected")) {
            menu.setSelected(String.valueOf(params.get("selected")));
        }
        if (params.containsKey("params")) {
            menu.setParams(String.valueOf(params.get("params")));
        }
        if (params.containsKey("is_cache")) {
            menu.setIsCache(intVal(params.get("is_cache"), 0));
        }
        if (params.containsKey("is_show")) {
            menu.setIsShow(intVal(params.get("is_show"), 1));
        }
        if (params.containsKey("is_disable")) {
            menu.setIsDisable(intVal(params.get("is_disable"), 0));
        }
        if (menu.getPid() == null) {
            menu.setPid(0);
        }
        if (!StringUtils.hasText(menu.getType())) {
            menu.setType("M");
        }
        return menu;
    }

    private int currentTs() {
        return (int) (System.currentTimeMillis() / 1000);
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
