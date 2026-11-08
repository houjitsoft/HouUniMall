package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.entity.mall.MallShopAdminRole;
import com.houjiang.merchant.common.entity.mall.MallShopMenu;
import com.houjiang.merchant.common.entity.mall.MallShopRoleMenu;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminRoleMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMenuMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopRoleMenuMapper;
import com.houjiang.merchant.common.util.ListUtils;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallShopAdminMenuService;
import com.houjiang.merchant.service.mall.IMallShopAdminService;
import com.houjiang.merchant.vo.mall.MallShopRoleMenuListVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统角色服务实现类
 * @author Amos
 */
@Service
public class MallShopAdminMenuServiceImpl implements IMallShopAdminMenuService {
    @Resource
    IMallShopAdminService iMallShopAdminService;

    @Resource
    MallShopMenuMapper mallShopMenuMapper;

    @Resource
    MallShopAdminRoleMapper mallShopAdminRoleMapper;
    @Resource
    MallShopRoleMenuMapper mallShopRoleMenuMapper;

    @Override
    public JSONArray getShopAdminMenuList(Integer adminId) {
        JSONArray ret = new JSONArray();
        MallShopAdmin shopAdmin = iMallShopAdminService.getById(adminId);
        if (StringUtils.isNull(shopAdmin)) {
            return ret;
        } else {
            //超管
            if (shopAdmin.getIsSuper().equals(YesNoEnum.YES.getCode())) {
                return this.getShopBaseList();
            } else {
                return this.getMenuListByAdminId(adminId);
            }
        }
    }

    @Override
    public List<String> getAdminAccessList(Integer adminId) {
        List<String> ret = new ArrayList<String>();
        MallShopAdmin shopAdmin = iMallShopAdminService.getById(adminId);
        if (StringUtils.isNull(shopAdmin)) {
            return ret;
        } else {
            //超管
            if (shopAdmin.getIsSuper().equals(YesNoEnum.YES.getCode())) {
                ret.add("*");
            } else {
                List<Integer> menuIDs = this.getMenuListIdsByAdminId(adminId);
                if (menuIDs.size() != 0) {
                    List<MallShopMenu> menuList = mallShopMenuMapper.selectList(new QueryWrapper<MallShopMenu>().eq("status", YesNoEnum.YES.getCode()).eq("delete_time", 0).orderByDesc("sort").orderByDesc("id").in("id", menuIDs).select("access").ne("access", ""));
                    if (menuList.size() > 0) {
                        for (MallShopMenu item : menuList) {
                            ret.add(item.getAccess());
                        }
                    }
                }
            }
            return ret;
        }
    }

    /**
     * 商家 权限列表
     * @return
     */
    private JSONArray getShopBaseList() {
        List<MallShopMenu> menuList = mallShopMenuMapper.selectList(new QueryWrapper<MallShopMenu>().eq("status", YesNoEnum.YES.getCode()).eq("delete_time", 0).orderByDesc("sort").orderByDesc("id"));

        List<MallShopRoleMenuListVo> lists = new ArrayList<>();
        for (MallShopMenu menu : menuList) {
            MallShopRoleMenuListVo vo = new MallShopRoleMenuListVo();
            BeanUtils.copyProperties(menu, vo);
            vo.setUpdateTime(TimeUtils.timestampToDate(menu.getUpdateTime()));
            vo.setCreateTime(TimeUtils.timestampToDate(menu.getCreateTime()));
            lists.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(lists));
        return ListUtils.listToTree(jsonArray, "id", "pid", "sub");
    }

    /**
     * 返回具体用户获取到的menulist
     * @return
     */
    private JSONArray getMenuListByAdminId(Integer adminId) {
        List<Integer> menuIDs = this.getMenuListIdsByAdminId(adminId);
        if (menuIDs.size() == 0) {
            return new JSONArray();
        }

        List<MallShopMenu> menuList = mallShopMenuMapper.selectList(new QueryWrapper<MallShopMenu>().eq("status", YesNoEnum.YES.getCode()).eq("delete_time", 0).orderByDesc("sort").orderByDesc("id").in("id", menuIDs));

        List<MallShopRoleMenuListVo> lists = new ArrayList<>();
        for (MallShopMenu menu : menuList) {
            MallShopRoleMenuListVo vo = new MallShopRoleMenuListVo();
            BeanUtils.copyProperties(menu, vo);
            vo.setUpdateTime(TimeUtils.timestampToDate(menu.getUpdateTime()));
            vo.setCreateTime(TimeUtils.timestampToDate(menu.getCreateTime()));
            lists.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(lists));
        return ListUtils.listToTree(jsonArray, "id", "pid", "sub");
    }

    /**
     * 根据商户管理员ID返回权限列表
     * @param adminId
     * @return
     */
    private List<Integer> getMenuListIdsByAdminId(Integer adminId) {
        List<MallShopAdminRole> roleList = mallShopAdminRoleMapper.selectList(
                new QueryWrapper<MallShopAdminRole>()
                        .eq("admin_id", adminId)
                        .and(query -> query.eq("delete_time", 0).or().isNull("delete_time"))
                        .select("role_id"));
        List<Integer> roleIDs = new ArrayList<Integer>();
        if (roleList.size() == 0) {
            return roleIDs;
        }

        for (MallShopAdminRole item : roleList) {
            roleIDs.add(item.getRoleId());
        }

        List<MallShopRoleMenu> roleMenus = mallShopRoleMenuMapper.selectList(
                new QueryWrapper<MallShopRoleMenu>()
                        .in("role_id", roleIDs)
                        .and(query -> query.eq("delete_time", 0).or().isNull("delete_time"))
                        .select("menu_id"));
        List<Integer> menuIDs = new ArrayList<Integer>();
        for (MallShopRoleMenu item : roleMenus) {
            menuIDs.add(item.getMenuId());
        }
        return menuIDs;
    }
}
