package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopRole;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopRoleMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopAdminRoleService;
import com.houjiang.merchant.service.mall.IMallShopRoleMenuService;
import com.houjiang.merchant.service.mall.IMallShopRoleService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleSetMenuValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopRoleDetailVo;
import com.houjiang.merchant.vo.mall.MallShopRoleListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 商家角色实现类
 * @author Amos
 */
@Service
public class MallShopRoleServiceImpl implements IMallShopRoleService {

    @Resource
    IMallShopRoleMenuService iMallShopRoleMenuService;
    @Resource
    IMallShopAdminRoleService iMallShopAdminRoleService;
    @Resource
    MallShopRoleMapper mallShopRoleMapper;

    /**
     * 商家角色列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopRoleListedVo>
     */
    @Override
    public PageResult<MallShopRoleListedVo> list(PageValidate pageValidate, MallShopRoleSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallShopRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_time", 0);
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));

        mallShopRoleMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:shopId@shop_id:int",
            "like:name:str",
            "=:sort:int",
            "=:desc:str",
            "=:status:int",
        });

        IPage<MallShopRole> iPage = mallShopRoleMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallShopRoleListedVo> list = new LinkedList<>();
        for(MallShopRole item : iPage.getRecords()) {
            MallShopRoleListedVo vo = new MallShopRoleListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setDesc(item.getDescField());
            vo.setNum(iMallShopAdminRoleService.getCountByRoleId(item.getId()));
            vo.setMenuId(iMallShopRoleMenuService.selectMenuIdsByRoleId(new ArrayList<Integer>(){{
                add(item.getId());
            }}));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 商家角色详情
     *
     * @author Amos
     * @param id 主键参数
     * @return MallShopRole
     */
    @Override
    public MallShopRoleDetailVo detail(Integer id) {
        MallShopRole model = mallShopRoleMapper.selectOne(
                new QueryWrapper<MallShopRole>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的角色信息");
        }

        MallShopRoleDetailVo vo = new MallShopRoleDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setDesc(model.getDescField());
        return vo;
    }

    /**
     * 商家角色新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(MallShopRoleCreateValidate createValidate) {
        MallShopRole model = new MallShopRole();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        // 数据库 delete_time 列允许 NULL 且无 DEFAULT 0,必须显式赋 0,否则列表 eq("delete_time", 0) 查不到
        model.setDeleteTime(0L);
        model.setShopId(createValidate.getShopId());
        model.setName(createValidate.getName());
        model.setSort(createValidate.getSort());
        model.setDescField(createValidate.getDesc());
        model.setStatus(createValidate.getStatus());
        mallShopRoleMapper.insert(model);
    }

    /**
     * 商家角色编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(MallShopRoleUpdateValidate updateValidate) {
        MallShopRole model = mallShopRoleMapper.selectOne(
                new QueryWrapper<MallShopRole>()
                    .eq("id",  updateValidate.getId()).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的角色信息");
        }

        model.setId(updateValidate.getId());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setName(updateValidate.getName());
        model.setSort(updateValidate.getSort());
        model.setDescField(updateValidate.getDesc());
        model.setStatus(updateValidate.getStatus());
        mallShopRoleMapper.updateById(model);
    }

    /**
     * 商家角色删除
     *
     * @author Amos
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        MallShopRole model = mallShopRoleMapper.selectOne(
                new QueryWrapper<MallShopRole>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能删除其他店铺的角色信息");
        }
        Boolean adminUse = iMallShopAdminRoleService.shopAdminUse(id);

        Assert.isTrue(adminUse, "该角色已被管理员使用,请先移除");

        model.setDeleteTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        mallShopRoleMapper.updateById(model);
    }

    @Override
    @Transactional
    public void setMenu(MallShopRoleSetMenuValidate menuValidate) {
        MallShopRole role = this.getById(menuValidate.getId());
        Assert.notNull(role, "找不到该角色的信息");
        if (role.getShopId().equals(menuValidate.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的角色菜单");
        }
        iMallShopRoleMenuService.batchDeleteByRoleId(menuValidate.getId());
        iMallShopRoleMenuService.batchSaveByMenuIds(menuValidate.getId(), menuValidate.getMenu(), menuValidate.getShopId());
    }

    @Override
    public MallShopRole getById(Integer id) {
        return mallShopRoleMapper.selectOne(new QueryWrapper<MallShopRole>().eq("id", id).eq("delete_time", 0));
    }

    @Override
    public List<MallShopRoleListedVo> otherLists(MallShopRoleSearchValidate searchValidate) {
        QueryWrapper<MallShopRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_time", 0);
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));

        mallShopRoleMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "=:shopId@shop_id:int",
                "like:name:str",
                "=:sort:int",
                "=:desc:str",
                "=:status:int",
        });

        List<MallShopRole> iPage = mallShopRoleMapper.selectList(queryWrapper);

        List<MallShopRoleListedVo> list = new LinkedList<>();
        for(MallShopRole item : iPage) {
            MallShopRoleListedVo vo = new MallShopRoleListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setDesc(item.getDescField());
            vo.setNum(iMallShopAdminRoleService.getCountByRoleId(item.getId()));
            vo.setMenuId(iMallShopRoleMenuService.selectMenuIdsByRoleId(new ArrayList<Integer>(){{
                add(item.getId());
            }}));
            list.add(vo);
        }

        return list;
    }
}
