package com.houjiang.job.service.impl;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.util.DateUtils;
import com.houjiang.job.dto.AppJiPushOrderDTO;
import com.houjiang.job.dto.BusinessOvertimeOrderDTO;
import com.houjiang.job.dto.OrderStatusDTO;
import com.houjiang.job.dto.RefundParamTwoDTO;
import com.houjiang.job.feign.BaseServiceClient;
import com.houjiang.job.feign.PayMentServiceClient;
import com.houjiang.job.mapper.UpdateOrderStatusMapper;
import com.houjiang.job.service.UpdateOrderStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Amos
 * @date 2023/5/17 14:32
 */
@Service
public class UpdateOrderStatusServiceImpl implements UpdateOrderStatusService {
    @Resource
    private UpdateOrderStatusMapper updateOrderStatusMapper;
    @Autowired
    private PayMentServiceClient paymentServiceClient;
    @Autowired
    private BaseServiceClient baseServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(UpdateOrderStatusService.class);

        /**
         * 查询待更新订单列表。
         *
         * @return 待更新订单列表。
         */
        @Override
        public List<AppJiPushOrderDTO> listForUpdateOrder(Integer automaticDeliveryTime) {
            return updateOrderStatusMapper.listForUpdateOrder(automaticDeliveryTime);
        }

        /**
         * 更新订单状态为待更新。
         *
         * @param orderId 订单ID。
         * @return 更新结果。
         */
        @Override
        public Integer updateForUpdateOrder(String orderId) {
            String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.AFTER_SALES_TIME.getDesc());
            Date orderLastReturnTime = DateUtils.addDays(DateUtils.getNowDateToDf(), Integer.parseInt(configValue));
            return updateOrderStatusMapper.updateForUpdateOrder(orderId,orderLastReturnTime);
        }

        /**
         * 更新订单中的商品状态为待更新。
         *
         * @param orderId 订单ID。
         * @return 更新结果。
         */
        @Override
        public Integer updateForUpdateGoods(String orderId) {
            return updateOrderStatusMapper.updateForUpdateGoods(orderId);
        }

        /**
         * 查询超时未更新的订单ID列表。
         *
         * @param minutes 超时分钟数。
         * @return 超时订单ID列表。
         */
        @Override
        public List<String> listTimeoutOrder2(String minutes) {
            return updateOrderStatusMapper.listTimeoutOrder2(minutes);
        }

        /**
         * 查询订单的商品销售汇总信息。
         *
         * @param orderId 订单ID。
         * @return 商品销售汇总信息列表。
         */
        @Override
        public List<WapGoodsSaleSumDTO> queryGoodsXinXi(String orderId) {
            return updateOrderStatusMapper.queryGoodsXinXi(orderId);
        }

        /**
         * 查询商品的通用库存。
         *
         * @param goodsId 商品ID。
         * @return 商品通用库存信息。
         */
        @Override
        public Map<String, Long> queryGoodsCommonStock(Integer goodsId) {
            return updateOrderStatusMapper.queryGoodsCommonStock(goodsId);
        }

        /**
         * 更新商品的通用库存。
         *
         * @param param 商品销售汇总信息。
         * @return 更新结果。
         */
        @Override
        public Integer updateCommonStockByGoodsId(WapGoodsSaleSumDTO param) {
            return updateOrderStatusMapper.updateCommonStockByGoodsId(param);
        }

        /**
         * 更新商品的库存。
         *
         * @param param 商品销售汇总信息。
         * @return 更新结果。
         */
        @Override
        public Integer updateGoodsStockByGoodsId(WapGoodsSaleSumDTO param) {
            return updateOrderStatusMapper.updateGoodsStockByGoodsId(param);
        }

        /**
         * 查询超时未更新的订单详情列表。
         *
         * @param minutes 超时分钟数。
         * @return 超时订单详情列表。
         */
        @Override
        public List<OrderStatusDTO> listTimeoutOrderDetail2(String minutes) {
            return updateOrderStatusMapper.listTimeoutOrderDetail2(minutes);
        }

        /**
         * 查询订单的支付状态。
         *
         * @param paymentNumber 支付编号。
         * @return 订单支付状态列表。
         */
        @Override
        public List<Integer> queryOrderStatus(String paymentNumber) {
            return updateOrderStatusMapper.queryOrderStatus(paymentNumber);
        }

        /**
         * 更新超时订单的状态。
         *
         * @param minutes 超时分钟数。
         * @return 更新结果。
         */
        @Override
        public Integer updateTimeoutOrder2(String minutes) {
            return updateOrderStatusMapper.updateTimeoutOrder2(minutes);
        }

        /**
         * 更新超时订单中的商品状态。
         *
         * @param orderId 订单ID。
         * @return 更新结果。
         */
        @Override
        public Integer updateTimeoutOrderGoods(String orderId) {
            return updateOrderStatusMapper.updateTimeoutOrderGoods(orderId);
        }

        /**
         * 修改订单的审批ID。
         *
         * @param orderId 订单ID。
         * @return 修改结果。
         */
        @Override
        public Integer editApprovalId(String orderId) {
            return updateOrderStatusMapper.editApprovalId(orderId);
        }


    /**
     * 处理超过48小时未处理的退款订单。
     * 自动将这些订单设置为退款完成状态，如果商家在指定时间内未操作。
     */
    @Override
    public void refundOrderHandleJob() {
        // 查询超过48小时未处理的退款订单
        List<BusinessOvertimeOrderDTO> pojoList = updateOrderStatusMapper.queryReturnOrderNotHandle();
        // 获取当前时间，用于设置订单的处理和完成时间
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String finishTime = dateformat.format(new Date());
        if (pojoList.size() >= 1) {
            for (BusinessOvertimeOrderDTO pojo : pojoList) {
                // 设置订单退款相关状态和信息
                pojo.setReturnShopHandle(5);
                pojo.setReturnState(5);
                pojo.setReturnShopMessage("超时未处理，自动同意");
                pojo.setReturnShopTime(finishTime);
                pojo.setReturnFinishTime(finishTime);
                pojo.setOrderStatus(6);
                pojo.setOrderRefundStatus(2);
                pojo.setOrderFinishedTime(finishTime);

                // 查询订单的支付编号
                String uOrderId = updateOrderStatusMapper.queryPaymentNumber(pojo);
                // 查询订单的交易状态
                List<Integer> orderTradeStatus = updateOrderStatusMapper.queryOrderRecordStatus(uOrderId);

                // 准备退款参数
                RefundParamDTO refundPara = new RefundParamDTO();
                boolean isRefundSuccess = false;
                String orderId = pojo.getOrderId();
                RefundParamTwoDTO refundParaTwo = updateOrderStatusMapper.queryRefundPara(orderId);
                refundPara.setOrderFrom(refundParaTwo.getOrderFrom());
                refundPara.setOrderId(refundParaTwo.getOrderId());
                refundPara.setOrderTradeStatus(orderTradeStatus);
                refundPara.setPayCode(refundParaTwo.getPayCode());
                refundPara.setReturnCode(refundParaTwo.getReturnCode());
                refundPara.setPaymentOtherNumber(refundParaTwo.getPaymentOtherNumber());
                // 实际发起退款操作
                isRefundSuccess = paymentServiceClient.waitShipmentsRefund(refundPara);
                if (isRefundSuccess == false) {
                    logger.info("====退款失败，跳过处理====");
                    continue;
                }else{
                    // 更新订单基础信息
                    // 更新订单表状态
                    Integer torf = updateOrderStatusMapper.updateBusinessOvertimeOrderBase(pojo);
                    // 更新订单退款信息
                    // 更新退款
                    updateOrderStatusMapper.updateBusinessOvertimeOrderReturn(pojo);
                }
            }
        }

        logger.info("已完成更新超过48小时商家未处理的售后退款订单。 更新时间: " + DateUtils.getNowDate());
    }

    @Override
    public void deleteTimeoutPlusOrder(String minutes) {
        updateOrderStatusMapper.deleteTimeoutPlusOrder(minutes);
    }

    @Override
    public void updateFxCommissionOrder(List<AppJiPushOrderDTO> list) {
        List<String> orderIds = list.stream().map(AppJiPushOrderDTO::getOrderId).collect(Collectors.toList());
        if(!orderIds.isEmpty()) {
            List<String> orderIdList = updateOrderStatusMapper.selectFxCommissionOrderId(orderIds);
            if(!orderIdList.isEmpty()){
                updateOrderStatusMapper.updateFxCommissionOrder(orderIdList);
            }
        }
    }

    /**
     * 付款凭证审核改为 审核驳回
     * @param orderId
     */
    @Override
    public void cancelOrderUpdatePaymentVocher(String orderId) {
        updateOrderStatusMapper.cancelOrderUpdatePaymentVocher(orderId);
    }
}
