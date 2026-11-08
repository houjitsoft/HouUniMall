package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.validate.mallorder.OrderAfterSaleLogCreateValidate;
import com.houjiang.merchant.vo.mall.MallOrderAfterSaleLogListedVo;
import com.houjiang.merchant.vo.mall.MallOrderLogListedVo;

import java.util.List;

/**
 *  退货订单日志接口类
 * @author Amos
 */
public interface IMallOrderAfteSaleLogService {

    /**
     * 保存日志
     * @param validate
     */
    void add(OrderAfterSaleLogCreateValidate validate);
    /**
     * 返回单位的配送列表
     * @param shopId
     * @return
     */
    List<MallOrderAfterSaleLogListedVo> getListByOId(Integer orderId);
}
