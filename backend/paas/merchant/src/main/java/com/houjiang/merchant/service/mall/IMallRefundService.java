package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallRefund;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallRefundReRefundValidate;
import com.houjiang.merchant.validate.mall.MallRefundSearchValidate;
import com.houjiang.merchant.vo.mall.MallOrderLogListedVo;
import com.houjiang.merchant.vo.mall.MallRefundListedVo;
import com.houjiang.merchant.vo.mall.MallRefundSumVo;
import com.houjiang.merchant.vo.mallorder.OrderAfterDetailVo;
import com.houjiang.merchant.vo.mallorder.OrderDetailVo;

import java.math.BigDecimal;
import java.util.List;

/**
 *  退款记录接口类
 */
public interface IMallRefundService {

    /**
     * @notes 创建售后退款
     * @author Amos
     * $params['refund_operator_id'] 操作人id
     * $params['refund_operator_type'] 操作人类型
     */
    void createRefundByOrderAfterSale(OrderAfterDetailVo detail, Integer shopId, Integer refundOperatorType);

    /**
     * @notes 创建订单取消退款
     * @author Amos
     */
    void createRefundByOrderCancel(OrderDetailVo detail, Integer refundType);

    /**
     * 返回店铺的所有退款金额
     * @param shopId
     * @return
     */
    BigDecimal getAllRefunMoneyByShopId(Integer shopId);

    /**
     * 返回店铺成功退款金额
     * @param shopId
     * @return
     */
    BigDecimal getRefunMoneyByShopId(Integer shopId);

    /**
     * 返回店铺的待退款金额
     * @param shopId
     * @return
     */
    BigDecimal getRefundWaitMoneyByShopId(Integer shopId);

    /**
     * 返回店铺成功退款金额
     * @param shopId
     * @return
     */
    BigDecimal getRefunFailedMoneyByShopId(Integer shopId);

    /**
     * 退款统计
     * @param shopId
     * @return
     */
    MallRefundSumVo sums(Integer shopId);

    /**
     * 退款记录列表
     * @param pageValidate
     * @param searchValidate
     * @return
     */
    PageResult<MallRefundListedVo> list(PageValidate pageValidate, MallRefundSearchValidate searchValidate);
    /**
     * @notes 重新退款
     * @author Amos
     */
    void reRefund(MallRefundReRefundValidate validate);

    /**
     * 处理待退款记录（将退款中状态标记为成功；无外部支付通道时做本地闭环）
     */
    void deal(MallRefundReRefundValidate validate);

    /**
     * 退款详情
     * @param id
     * @return
     */
    MallRefund getById(Integer id);
}
