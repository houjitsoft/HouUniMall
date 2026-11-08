package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.validate.mall.MallShopUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopDetailVo;
import com.houjiang.merchant.vo.mall.MallShopSimplifyDetailVo;

import java.math.BigDecimal;

/**
 *  商家店铺接口类
 */
public interface IMallShopService {

    /**
     * 获取详细
     * @param id
     * @return
     */
    MallShopDetailVo detail(Integer id);

    /**
     * 获取精简详细
     * @param id
     * @return
     */
    MallShopSimplifyDetailVo simplifyDetail(Integer id);

    /**
     * 根据ID返回信息
     * @param id
     * @return
     */
    MallShop getById(Integer id);

    /**
     * 更新 店铺在售商品数
     */
    void updateOnlineGoods(Integer shopId);

    /**
     * @notes 扣减店铺余额
     * @param int $id
     * @param float $money
     * @return bool
     * @author Amos
     */
    Boolean decMoney(Integer shopId, BigDecimal money, Boolean checkMoney);

    /**
     * 根据ID返回店铺名称
     * @param shopId
     * @return
     */
    String getNameById(Integer shopId);

    /**
     * 商家店铺编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    void edit(MallShopUpdateValidate updateValidate);

    /**
     * @notes 扣减店铺采集余额
     * @param int $id
     * @param float $money
     * @return bool
     * @author Amos
     */
    Boolean decGatherMoney(Integer shopId, Integer gatherPrice);
}
