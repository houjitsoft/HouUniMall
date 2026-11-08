package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopAdminCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopAdminSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopAdminUpdateValidate;
import com.houjiang.merchant.validate.shop.MallShopAccountUpdateValidate;
import com.houjiang.merchant.vo.admin.AdminAccountDetailVo;
import com.houjiang.merchant.vo.mall.MallShopAdminListedVo;
import com.houjiang.merchant.vo.mall.MallShopAdminDetailVo;

import java.util.List;
import java.util.Map;

/**
 *  商家账号接口类
 */
public interface IMallShopAdminService {

    /**
     *  返回账号信息
     * @param adminId
     */
    AdminAccountDetailVo accountDetail(Integer adminId);

    /**
     * 根据ID返回信息
     * @param id
     * @return
     */
    MallShopAdmin getById(Integer id);

    /**
     * 更新账号信息
     * @param id
     * @return
     */
    void accountUpdate(MallShopAccountUpdateValidate mallShopAccountUpdateValidate, Integer id);


    /**
     * 商家账号列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<LsMallShopAdminListedVo>
     */
    PageResult<MallShopAdminListedVo> list(PageValidate pageValidate, MallShopAdminSearchValidate searchValidate);

    /**
     * 商家账号全部列表（不分页，供下拉选择）
     */
    List<MallShopAdminListedVo> all(MallShopAdminSearchValidate searchValidate);

    List<Map<String, Object>> accountOptions(String keyword);

    /**
     * 商家账号详情
     *
     * @author Amos
     * @param id 主键ID
     * @return LsMallShopAdminDetailVo
     */
    MallShopAdminDetailVo detail(Integer id);

    /**
     * 商家账号新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(MallShopAdminCreateValidate createValidate);

    /**
     * 商家账号编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(MallShopAdminUpdateValidate updateValidate);

    /**
     * 商家账号删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);

}
