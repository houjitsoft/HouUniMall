package com.houjiang.merchant.service.mall;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallOrder;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mallorder.MallOrderSendDeliveryValidate;
import com.houjiang.merchant.validate.mallorder.OrderSearchValidate;
import com.houjiang.merchant.vo.mallorder.LogisticsDetailVo;
import com.houjiang.merchant.vo.mallorder.OrderAfterOrderDetailVo;
import com.houjiang.merchant.vo.mallorder.OrderDetailVo;
import com.houjiang.merchant.vo.mallorder.OrderListedVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 *  订单表接口类
 */
public interface IMallOrderService {

    /**
     * @notes 营业额
     * @param int $day
     * @param int|null $shop_id
     * @return array
     * @author Amos
     */
    JSONObject saleDay(Integer day, Integer shopId);

    /**
     * 订单列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param orderSearchValidate 搜素参数
     * @return PageResult<OrderListVo>
     */
    PageResult<OrderListedVo> list(PageValidate pageValidate, OrderSearchValidate orderSearchValidate);

    /**
     * 订单详情
     * @param id
     * @return
     */
    OrderDetailVo detail(Integer id);

    /**
     * 订单备注
     *
     * @author Amos
     * @param orderId 订单ID
     * @param remarks 备注信息
     */
    void setRemarks(Integer orderId, String remarks);

    /**
     * 根据ID返回
     * @param id
     * @return
     */
    MallOrder getById(Integer id);

    /**
     * 订单发货
     *
     * @author Amos
     * @param sendDeliveryValidate 发货参数
     */
    void sendDelivery(MallOrderSendDeliveryValidate sendDeliveryValidate);
    /**
     * 确认收货
     *
     * @author Amos
     * @param orderId 订单ID
     */
    void takeDelivery(Integer id);

    /**
     * 获取当前售后时间
     *
     * @author Amos
     * @return Long
     */
    Long getAfterSaleDeadline();

    /**
     * 查看物流
     *
     * @author Amos
     * @param orderId 订单ID
     * @return Map<String, Object>
     */
    LogisticsDetailVo logistics(Integer id);

    /**
     * 取消订单
     *
     * @author Amos
     * @param id Integer
     */
    void cancel(Integer id);

    /**
     * 获取退货订单详情
     * @param id
     * @return
     */
    OrderAfterOrderDetailVo getAfterOrderDetailById(Integer id);

    /**
     * @notes 检测售后状态
     * @return void
     * @author Amos
     */
    void chechAfterSaleSave(Integer orderId);

    /**
     * 成交订单数
     * @param shopId
     * @return
     */
    Integer getPayIsCount(Integer shopId);

    /**
     * 返回待结算的订单数量
     * @param shopId
     * @return
     */
    Integer getWaitSettlementAllOrderNum(Integer shopId);

    /**
     * 返回待结算的订单金额
     * @param shopId
     * @return
     */
    BigDecimal getWaitSettlementAllOrderMoeny(Integer shopId);

    /**
     * 打印小票
     * @param id
     */
    void print(Integer id);
}
