package com.houjiang.job.job;


import com.houjiang.commons.util.DateUtils;
import com.houjiang.job.dto.BusinessOvertimeOrderDTO;
import com.houjiang.job.mapper.UpdateOrderStatusMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退货订单超24小时商家未处理,自动同意
 * @author Amos
 */
public class ReturnOrderHandleJob implements BaseJob {
    /**
     * 定义一个日志器，用于记录退款订单处理作业的相关信息。
     */
    private static final Logger logger = LoggerFactory.getLogger(RefundOrderHandleJob.class);

    /**
     * 自动注入UpdateOrderStatusMapper，用于更新订单状态。
     */
    @Autowired
    UpdateOrderStatusMapper updateOrderStatusMapper;

    /**
     * 执行退款订单处理作业。
     * 本方法查询超过24小时未处理的退款订单，并将这些订单的状态更新为商家同意退款。
     *
     * @param context 上下文对象，包含作业执行的相关信息。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 查询超过24小时未处理的退款订单
        List<BusinessOvertimeOrderDTO> pojoList = updateOrderStatusMapper.queryTuiHuoOrderNotHandle();
        // 格式化当前日期，用于记录操作时间
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String finishTime = dateformat.format(new Date());

        // 初始化订单状态更新参数
        Map<String, Object> orderGoodsMap = new HashMap<String, Object>();
        // 遍历查询到的退款订单，准备更新订单状态
        for (BusinessOvertimeOrderDTO pojo : pojoList) {
            // 设置订单状态更新的具体参数
            orderGoodsMap.put("returnCode", pojo.getReturnCode());
            orderGoodsMap.put("returnShopHandle", 2);
            orderGoodsMap.put("returnState", 2);
            orderGoodsMap.put("returnShopMessage", "商家超时未处理,自动同意-->买家填写物流");
            orderGoodsMap.put("returnShopTime", finishTime);
            // 更新订单状态
            updateOrderStatusMapper.updateOrderReturnById(orderGoodsMap);
        }
        // 记录订单状态更新操作的时间
        logger.info("更新超过24小时商家未处理售后退货订单  更新时间" + DateUtils.getNowDate());
    }


}
