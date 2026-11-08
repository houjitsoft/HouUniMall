package com.houjiang.job.job;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.job.dto.OrderStatusDTO;
import com.houjiang.job.entity.JobPlatformEntity;
import com.houjiang.job.feign.ActivityServiceClient;
import com.houjiang.job.feign.GoodsServiceClient;
import com.houjiang.job.service.JobPlatformService;
import com.houjiang.job.service.UpdateOrderStatusService;
import com.houjiang.job.service.UpdateRecordStatusService;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 更新超过1天 未支付订单
 * @author Amos
 */
public class UpdateTimeoutOrderStatusJob implements BaseJob {

    private static final Logger logger = LoggerFactory.getLogger(UpdateTimeoutOrderStatusJob.class);

    @Autowired
    private UpdateOrderStatusService updateOrderstatusService;

    @Autowired
    private UpdateRecordStatusService updateRecordStatusService;

    @Autowired
    private ActivityServiceClient activityServiceClient;

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Autowired
    private JobPlatformService jobPlatformService;

    /** 1.1 返回超时订单红包 */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 设置超时时间，单位为分钟
        Integer time = 60 * 24;
        String minutes = time.toString();
        // 从配置中获取超时时间，如果存在则覆盖默认值
        JobPlatformEntity jobPlatformEntity = jobPlatformService.getOne(new LambdaQueryWrapper<JobPlatformEntity>()
                .eq(JobPlatformEntity::getInvokeTarget, "com.houjiang.job.job.UpdateTimeoutOrderStatusJob"));
        String timeOutPeriod = jobPlatformEntity.getTimeOutPeriod();
        if (!StringUtils.isEmpty(timeOutPeriod)) {
            minutes = timeOutPeriod;
        }
        //查询超过n分钟额订单
        List<String> orderIds = updateOrderstatusService.listTimeoutOrder2(minutes);
        // 遍历超时订单，查询并累加每个订单的商品销售信息
        List<WapGoodsSaleSumDTO> goodsDetailsList = new ArrayList<WapGoodsSaleSumDTO>();
        for (String orderId : orderIds) {
            List<WapGoodsSaleSumDTO> wapGood = updateOrderstatusService.queryGoodsXinXi(orderId);
            for (WapGoodsSaleSumDTO wapOne : wapGood) {
                goodsDetailsList.add(wapOne);
            }
        }
        // 更新商品库存信息
        goodsServiceClient.updateGoodsStock(goodsDetailsList);
        // 查询并处理超时订单的详细信息，包括红包和优惠券的退还
        List<OrderStatusDTO> orderStatus = updateOrderstatusService.listTimeoutOrderDetail2(minutes);
        for (OrderStatusDTO orderStatu : orderStatus) {
            // 退还红包，如果存在
            if (orderStatu.getRedpacketCode() != null) {
                // 查询订单状态
                List<Integer> orderStatusList = updateOrderstatusService.queryOrderStatus(orderStatu.getPaymentNumber());
                Map<String, Object> codeMap = new HashMap<String, Object>();
                codeMap.put("redpacketId", orderStatu.getRedpacketCode());
                codeMap.put("platformVoucherCode", orderStatu.getPlatformCouponCode());
                codeMap.put("type", "1");
                Set set = new HashSet(orderStatusList);
                if (set.size() == 1) {
                    activityServiceClient.updateSalesPrm(codeMap);
                    // 退还优惠券，如果存在
                    if (orderStatu.getVoucherCode() != null && !orderStatu.getVoucherCode().isEmpty()) {
                        List<String> voucherCodeList = Arrays.asList(orderStatu.getVoucherCode().split(","));
                        Map<String, Object> voucherCodeMap = new HashMap<String, Object>();
                        voucherCodeMap.put("voucherId", voucherCodeList);
                        voucherCodeMap.put("type", "1");
                        activityServiceClient.updateSalesPrm(voucherCodeMap);
                    }
                }
            } else {
                // 仅退还优惠券，如果存在
                if (orderStatu.getVoucherCode() != null && !orderStatu.getVoucherCode().isEmpty()) {
                    List<String> voucherCodeList = Arrays.asList(orderStatu.getVoucherCode().split(","));
                    Map<String, Object> voucherCodeMap = new HashMap<String, Object>();
                    voucherCodeMap.put("voucherId", voucherCodeList);
                    voucherCodeMap.put("platformVoucherCode", orderStatu.getPlatformCouponCode());
                    voucherCodeMap.put("type", "1");
                    activityServiceClient.updateSalesPrm(voucherCodeMap);
                }
            }
            // 判断是否是线下支付 如果是 取消订单同时 付款凭证审核改为 审核驳回
            if(CommonType.payType.OFFLINE_PAY.getCode().equals(orderStatu.getPaymentCode())){
                updateOrderstatusService.cancelOrderUpdatePaymentVocher(orderStatu.getOrderId());
            }
        }
        // 记录待更新的超时订单号
        // 更新超时订单的状态
        // 更新超过24小时未支付订单（orderBase表）
        Integer countTimeoutOrderBase = updateOrderstatusService.updateTimeoutOrder2(minutes);
        logger.info("更新所有超时未支付订单(orderBase表)，更新数量为：" + countTimeoutOrderBase);
        // 更新超过24小时未支付订单（orderGoods表）
        logger.info("更新所有超时未支付订单(ordergoods表)");
        for (String orderId : orderIds) {
            updateOrderstatusService.updateTimeoutOrderGoods(orderId);
            // 更新所有超时未支付订单关联的审批表(odb_order_approval表)
            // updateOrderstatusService.editApprovalId(orderId);
        }
        // 更新超时订单相关的记录表和交易表
        // 根据paymentOrder更新所有超时订单记录(Record表)
        Integer countRecord = 0;
        for (String orderId : orderIds) {
            countRecord += updateRecordStatusService.updateTimeoutRecord(orderId);
        }
        logger.info("更新所有超时未支付订单记录，更新数量为：" + countRecord);
        // 根据paymentOrder更新所有超时订单记录(Trade表)
        logger.info("更新所有超时未支付订单Trade：");
        for (String orderId : orderIds) {
            countRecord += updateRecordStatusService.updateTimeoutTrade(orderId);
        }
        // 删除过期未支付的PLUS预支付订单
        updateOrderstatusService.deleteTimeoutPlusOrder(minutes);
    }

}
