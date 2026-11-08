package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopBank;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopBankCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopBankSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopBankUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopBankDetailVo;
import com.houjiang.merchant.vo.mall.MallShopBankListedVo;

/**
 * 商家银行卡服务接口类
 * @author Amos
 */
public interface IMallShopBankService {

    /**
     * 商家银行卡列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopBankListedVo>
     */
    PageResult<MallShopBankListedVo> list(PageValidate pageValidate, MallShopBankSearchValidate searchValidate);

    /**
     * 商家银行卡详情
     *
     * @author Amos
     * @param id 主键ID
     * @return MallShopBankDetailVo
     */
    MallShopBankDetailVo detail(Integer id);

    /**
     * 商家银行卡新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    void add(MallShopBankCreateValidate createValidate);

    /**
     * 商家银行卡编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(MallShopBankUpdateValidate updateValidate);

    /**
     * 商家银行卡删除
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
    MallShopBank getById(Integer id);

}
