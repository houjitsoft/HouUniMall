package com.houjiang.merchant.service.impl.system;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.system.ISystemMenuService;
import com.houjiang.merchant.service.system.ISystemRoleMenuService;
import com.houjiang.merchant.validate.system.SystemMenuCreateValidate;
import com.houjiang.merchant.validate.system.SystemMenuUpdateValidate;
import com.houjiang.merchant.vo.system.SystemAuthMenuVo;
import com.houjiang.merchant.vo.system.SystemMenuListedVo;
import com.houjiang.merchant.common.entity.system.SystemMenu;
import com.houjiang.merchant.common.mapper.system.SystemMenuMapper;
import com.houjiang.merchant.common.util.ListUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 系统菜单服务实现类
 */
@Service
public class SystemMenuServiceImpl implements ISystemMenuService {

    @Resource
    SystemMenuMapper systemAuthMenuMapper;

    @Resource
    ISystemRoleMenuService iSystemRoleMenuService;

    /**
     * 根据角色ID获取菜单
     *
     * @author Amos
     * @param roleIds 角色ID
     * @return JSONArray
     */
    @Override
    public JSONArray selectMenuByRoleId(List<Integer> roleIds) {
        Integer adminId = MerchantThreadLocal.getAdminId();
        List<Integer> menuIds = iSystemRoleMenuService.selectMenuIdsByRoleId(roleIds);

        QueryWrapper<SystemMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("type", Arrays.asList("M", "C"));
//        queryWrapper.eq("is_disable", 0);
        queryWrapper.orderByDesc("sort");
        queryWrapper.orderByAsc("id");
        if (!adminId.equals(1)) {
            if (menuIds.size() <= 0) {
                menuIds.add(0);
            }
            queryWrapper.in("id", menuIds);
        }

        List<SystemMenu> systemAuthMenus = systemAuthMenuMapper.selectList(queryWrapper);

        List<SystemAuthMenuVo> lists = new ArrayList<>();
        for (SystemMenu systemAuthMenu : systemAuthMenus) {
            SystemAuthMenuVo vo = new SystemAuthMenuVo();
            BeanUtils.copyProperties(systemAuthMenu, vo);
            vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthMenu.getUpdateTime()));
            vo.setCreateTime(TimeUtils.timestampToDate(systemAuthMenu.getCreateTime()));
            lists.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(lists));
        return ListUtils.listToTree(jsonArray, "id", "pid", "children");
    }

    /**
     * 菜单列表
     *
     * @author Amos
     * @return JSONArray
     */
    @Override
    public JSONObject list() {
        return new JSONObject() {{
           put("lists", all());
        }};
    }

    @Override
    public JSONArray systemMenuLists() {
        QueryWrapper<SystemMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("sort");
        queryWrapper.orderByAsc("id");

        List<SystemMenu> systemAuthMenus = systemAuthMenuMapper.selectList(queryWrapper);

        List<SystemMenuListedVo> lists = new ArrayList<>();
        for (SystemMenu systemAuthMenu : systemAuthMenus) {
            SystemMenuListedVo vo = new SystemMenuListedVo();
            BeanUtils.copyProperties(systemAuthMenu, vo);

            vo.setCreateTime(TimeUtils.timestampToDate(systemAuthMenu.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthMenu.getUpdateTime()));
            lists.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(lists));
        return  ListUtils.listToTree(jsonArray, "id", "pid", "children");
    }

    @Override
    public JSONArray all() {
        QueryWrapper<SystemMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("sort");
        queryWrapper.orderByAsc("id");

        List<SystemMenu> systemAuthMenus = systemAuthMenuMapper.selectList(queryWrapper);

        List<SystemAuthMenuVo> lists = new ArrayList<>();
        for (SystemMenu systemAuthMenu : systemAuthMenus) {
            SystemAuthMenuVo vo = new SystemAuthMenuVo();
            BeanUtils.copyProperties(systemAuthMenu, vo);

            vo.setCreateTime(TimeUtils.timestampToDate(systemAuthMenu.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthMenu.getUpdateTime()));
            lists.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(lists));

        return  ListUtils.listToTree(jsonArray, "id", "pid", "children");
    }

    /**
     * 菜单详情
     *
     * @author Amos
     * @param id 主键参数
     * @return SysMenu
     */
    @Override
    public SystemAuthMenuVo detail(Integer id) {
        SystemMenu systemAuthMenu = systemAuthMenuMapper.selectOne(new QueryWrapper<SystemMenu>().eq("id", id));
        Assert.notNull(systemAuthMenu, "菜单已不存在!");

        SystemAuthMenuVo vo  = new SystemAuthMenuVo();
        BeanUtils.copyProperties(systemAuthMenu, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(systemAuthMenu.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthMenu.getUpdateTime()));

        return vo;
    }

    /**
     * 新增菜单
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(SystemMenuCreateValidate createValidate) {
        SystemMenu model = new SystemMenu();
        model.setPid(createValidate.getPid());
        model.setType(createValidate.getType());
        model.setName(createValidate.getName());
        model.setIcon(createValidate.getIcon());
        model.setSort(createValidate.getSort());
        model.setPerms(createValidate.getPerms());
        model.setPaths(createValidate.getPaths());
        model.setComponent(createValidate.getComponent());
        model.setSelected(createValidate.getSelected());
        model.setParams(createValidate.getParams());
        model.setIsCache(createValidate.getIsCache());
        model.setIsShow(createValidate.getIsShow());
        model.setIsDisable(createValidate.getIsDisable());
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        systemAuthMenuMapper.insert(model);
    }

    /**
     * 编辑菜单
     *
     * @author Amos
     * @param updateValidate 菜单
     */
    @Override
    public void edit(SystemMenuUpdateValidate updateValidate) {
        SystemMenu model = systemAuthMenuMapper.selectOne(new QueryWrapper<SystemMenu>().eq("id", updateValidate.getId()));
        Assert.notNull(model, "菜单已不存在!");

        model.setType(updateValidate.getType());
        model.setName(updateValidate.getName());
        model.setIcon(updateValidate.getIcon());
        model.setSort(updateValidate.getSort());
        model.setPaths(updateValidate.getPaths());
        model.setPerms(updateValidate.getPerms());
        model.setComponent(updateValidate.getComponent());
        model.setPid(updateValidate.getPid());
        model.setSelected(updateValidate.getSelected());
        model.setParams(updateValidate.getParams());
        model.setIsCache(updateValidate.getIsCache());
        model.setIsShow(updateValidate.getIsShow());
        model.setIsDisable(updateValidate.getIsDisable());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        systemAuthMenuMapper.updateById(model);
    }

    /**
     * 删除菜单
     *
     * @author Amos
     * @param id 主键参数
     */
    @Override
    public void del(Integer id) {
        SystemMenu model = systemAuthMenuMapper.selectOne(
                new QueryWrapper<SystemMenu>()
                        .select("id,pid,name")
                        .eq("id", id)
                        .last("limit 1"));

        Assert.notNull(model, "菜单已不存在!");
        Assert.isNull(systemAuthMenuMapper.selectOne(
                new QueryWrapper<SystemMenu>()
                        .eq("pid", id)
                        .last("limit 1")),
                "请先删除子菜单再操作!");

        systemAuthMenuMapper.deleteById(id);
        iSystemRoleMenuService.batchDeleteByMenuId(id);
    }

}
