package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopWithdraw;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopWithdrawAddValidate;
import com.houjiang.merchant.validate.mall.MallShopWithdrawSearchValidate;
import com.houjiang.merchant.vo.mall.MallShopWithdrawDetailVo;
import com.houjiang.merchant.vo.mall.MallShopWithdrawListedVo;

import java.math.BigDecimal;

/**
 *  商家提现接口类
 * @author Amos
 */
public interface IMallShopWithdrawService {

    /**
     * 提现中金额
     * @param shopId
     * @return
     */
    BigDecimal getWithdrawMoney(Integer shopId);

    /**
     * 提现手续费
     * @param shopId
     * @return
     */
    BigDecimal getWithdrawServiceMoney(Integer shopId);

    /**
     * 提现成功金额
     * @param shopId
     * @return
     */
    BigDecimal getWithdrawSuccessMoney(Integer shopId);

    /**
     * 获取商家提现列表
     * @param pageValidate
     * @param searchValidate
     * @return
     */
    PageResult<MallShopWithdrawListedVo> list(PageValidate pageValidate, MallShopWithdrawSearchValidate searchValidate);

    /**
     * 获取商家提现详情
     * @param id
     * @return
     */
    MallShopWithdrawDetailVo detail(Integer id);

    /**
     * 根据ID返回信息
     * @param id
     * @return
     */
    MallShopWithdraw getById(Integer id);

    /**
     * 添加商家提现
     * @param validate
     */
    void add(MallShopWithdrawAddValidate validate);
}
