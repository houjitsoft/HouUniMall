package com.houjiang.job.job;


import com.houjiang.commons.constant.CommonType;
import com.houjiang.job.dto.AppJiPushOrderDTO;
import com.houjiang.job.feign.BaseServiceClient;
import com.houjiang.job.service.UpdateOrderStatusService;
import com.houjiang.job.service.UpdateRecordStatusService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Amos
 */
public class UpdateReceiverOrderStatusJob implements BaseJob {

    private static final Logger logger = LoggerFactory.getLogger(UpdateReceiverOrderStatusJob.class);

    @Resource
    private UpdateOrderStatusService updateOrderstatusService;

    @Resource
    private UpdateRecordStatusService updateRecordStatusService;

    @Resource
    private BaseServiceClient baseServiceClient;

    /** 更新已收货 ，但是超过时间未确认收货订单 */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //KdniaoTrackQueryAPI api = new KdniaoTrackQueryAPI();
        String configValue = baseServiceClient.selectConfigValueFeign(CommonType.configKey.AUTOMATIC_DELIVERY_TIME.getDesc());
        Integer automaticDeliveryTime = Integer.parseInt(configValue);
        //获取待更新状态订单的 paymentNumber
        List<AppJiPushOrderDTO> orderIds = updateOrderstatusService.listForUpdateOrder(automaticDeliveryTime);

        //更新所有待超时订单
        Integer countOrder = 0;

        for (AppJiPushOrderDTO orderId : orderIds) {
            logger.info("更新所有超时未确认收货订单相关记录，订单号为：" + orderId.getOrderId());
            countOrder += updateOrderstatusService.updateForUpdateOrder(orderId.getOrderId());
        }
        logger.info("更新所有超时未确认收货订单，更新数量为：" + countOrder);

        //更新所有待超时订单(orderGoods表)
        for (AppJiPushOrderDTO orderId : orderIds) {
            updateOrderstatusService.updateForUpdateGoods(orderId.getOrderId());
        }

        //更新所有待超时订单(order_commission表)
        if (!orderIds.isEmpty()) {
            updateOrderstatusService.updateFxCommissionOrder(orderIds);
        }


        //更新所有超时订单相关的记录
        //当trade_type_id = 1，即为购物时再更新
        Integer countRecord = 0;
        for (AppJiPushOrderDTO orderId : orderIds) {
            countRecord += updateRecordStatusService.updateReceiverdRecord(orderId.getOrderId());
        }
        logger.info("更新所有超时未确认收货订单相关记录，更新数量为：" + countRecord);

        logger.info("更新所有超时未确认收货订单相关Trade");
        for (AppJiPushOrderDTO orderId : orderIds) {
            countRecord += updateRecordStatusService.updateReceiverdTrade(orderId.getOrderId());
        }
    }

}
