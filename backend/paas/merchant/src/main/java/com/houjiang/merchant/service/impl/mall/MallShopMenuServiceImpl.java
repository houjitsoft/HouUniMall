package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.common.entity.mall.MallShopMenu;
import com.houjiang.merchant.common.mapper.mall.MallShopMenuMapper;
import com.houjiang.merchant.common.util.ListUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopMenuService;
import com.houjiang.merchant.service.mall.IMallShopRoleMenuService;
import com.houjiang.merchant.vo.mall.MallShopMenuListedVo;
import com.houjiang.merchant.vo.mall.MallShopMenuVo;
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
public class MallShopMenuServiceImpl implements IMallShopMenuService {


    @Resource
    MallShopMenuMapper mallShopMenuMapper;


    @Resource
    IMallShopRoleMenuService iMallShopRoleMenuService;

    /**
     * 根据角色ID获取菜单
     *
     * @author Amos
     * @param roleIds 角色ID
     * @return JSONArray
     */
    @Override
    public JSONArray selectMenuByRoleId(List<Integer> roleIds) {
        Integer isSuper = MerchantThreadLocal.getIsSuper();
        List<Integer> menuIds = iMallShopRoleMenuService.selectMenuIdsByRoleId(roleIds);

        QueryWrapper<MallShopMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("type", Arrays.asList("M", "C"));
        queryWrapper.orderByDesc("sort");
        queryWrapper.orderByAsc("id");
        if (!isSuper.equals(1)) {
            if (menuIds.size() <= 0) {
                menuIds.add(0);
            }
            queryWrapper.in("id", menuIds);
        }

        List<MallShopMenu> mallShopMenus = mallShopMenuMapper.selectList(queryWrapper);

        List<MallShopMenuVo> lists = new ArrayList<>();
        for (MallShopMenu mallShopMenu : mallShopMenus) {
            MallShopMenuVo vo = new MallShopMenuVo();
            BeanUtils.copyProperties(mallShopMenu, vo);
            vo.setUpdateTime(TimeUtils.timestampToDate(mallShopMenu.getUpdateTime()));
            vo.setCreateTime(TimeUtils.timestampToDate(mallShopMenu.getCreateTime()));
            lists.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(lists));
        return ListUtils.listToTree(jsonArray, "id", "pid", "sub");
    }

    /**
     * 菜单列表
     *
     * @author Amos
     * @return JSONArray
     */
    @Override
    public JSONArray list() {
        return all();
    }

    @Override
    public JSONArray systemMenuLists() {
        QueryWrapper<MallShopMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("sort");
        queryWrapper.orderByAsc("id");

        List<MallShopMenu> mallShopMenus = mallShopMenuMapper.selectList(queryWrapper);

        List<MallShopMenuListedVo> lists = new ArrayList<>();
        for (MallShopMenu mallShopMenu : mallShopMenus) {
            MallShopMenuListedVo vo = new MallShopMenuListedVo();
            BeanUtils.copyProperties(mallShopMenu, vo);

            vo.setCreateTime(TimeUtils.timestampToDate(mallShopMenu.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(mallShopMenu.getUpdateTime()));
            lists.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(lists));
        return  ListUtils.listToTree(jsonArray, "id", "pid", "sub");
    }

    @Override
    public JSONArray all() {
        QueryWrapper<MallShopMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("sort");
        queryWrapper.orderByAsc("id");

        List<MallShopMenu> mallShopMenus = mallShopMenuMapper.selectList(queryWrapper);

        List<MallShopMenuVo> lists = new ArrayList<>();
        for (MallShopMenu mallShopMenu : mallShopMenus) {
            MallShopMenuVo vo = new MallShopMenuVo();
            BeanUtils.copyProperties(mallShopMenu, vo);

            vo.setCreateTime(TimeUtils.timestampToDate(mallShopMenu.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(mallShopMenu.getUpdateTime()));
            lists.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(lists));

        return  ListUtils.listToTree(jsonArray, "id", "pid", "sub");
    }

    /**
     * 菜单详情
     *
     * @author Amos
     * @param id 主键参数
     * @return SysMenu
     */
    @Override
    public MallShopMenuVo detail(Integer id) {
        MallShopMenu mallShopMenu = mallShopMenuMapper.selectOne(new QueryWrapper<MallShopMenu>().eq("id", id));
        Assert.notNull(mallShopMenu, "菜单已不存在!");

        MallShopMenuVo vo  = new MallShopMenuVo();
        BeanUtils.copyProperties(mallShopMenu, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(mallShopMenu.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(mallShopMenu.getUpdateTime()));

        return vo;
    }
}
