package com.houjiang.merchant.service.mall;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopBank;
import com.houjiang.merchant.common.entity.mall.MallShopDecorate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.*;
import com.houjiang.merchant.vo.mall.*;

/**
 * 商家装修服务接口类
 * @author Amos
 */
public interface IMallShopDecorateService {

    /**
     * 店铺装修列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopBankListedVo>
     */
    PageResult<MallShopDecorateListedVo> list(PageValidate pageValidate, MallShopDecorateSearchValidate searchValidate);

    /**
     * 店铺装修详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallShopBankDetailVo
     */
    MallShopDecorateDetailVo detail(Integer id);

    /**
     * 店铺装修新增
     *
     * @author Amos
     * @param createValidate 参数
     * @return 新记录ID
     */
    Integer add(MallShopDecorateCreateValidate createValidate);

    /**
     * 店铺装修编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(MallShopDecorateUpdateValidate updateValidate);

    /**
     * 店铺装修删除
     *
     * @author Amos
     * @param id 主键ID
     */
    void del(Integer id);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    MallShopDecorate getById(Integer id);

    /**
     * 设置首页
     * @param id
     */
    void setHome(Integer id);
}
