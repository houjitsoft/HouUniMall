package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopRole;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleSetMenuValidate;
import com.houjiang.merchant.validate.mall.MallShopRoleUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopRoleDetailVo;
import com.houjiang.merchant.vo.mall.MallShopRoleListedVo;

import java.util.List;

/**
 * 商家角色服务接口类
 * @author Amos
 */
public interface IMallShopRoleService {

    /**
     * 商家角色列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopRoleListedVo>
     */
    PageResult<MallShopRoleListedVo> list(PageValidate pageValidate, MallShopRoleSearchValidate searchValidate);

    /**
     * 商家角色详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallShopRoleDetailVo
     */
    MallShopRoleDetailVo detail(Integer id);

    /**
     * 商家角色新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(MallShopRoleCreateValidate createValidate);

    /**
     * 商家角色编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(MallShopRoleUpdateValidate updateValidate);

    /**
     * 商家角色删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);

    /**
     * 设置菜单
     * @param menuValidate
     */
    void setMenu(MallShopRoleSetMenuValidate menuValidate);

    /**
     * 获取角色
     * @param id
     * @return
     */
    MallShopRole getById(Integer id);

    /**
     * 商家角色列表
     * @author Amos
     * @param searchValidate 搜索参数
     * @return List<MallShopRoleListedVo>
     */
    List<MallShopRoleListedVo> otherLists(MallShopRoleSearchValidate searchValidate);
}
