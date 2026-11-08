package com.houjiang.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.job.entity.JobDayOrderStaticsEntity;
import com.houjiang.job.mapper.JobDayOrderStaticsMapper;
import com.houjiang.job.service.JobDayOrderStaticsService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class JobDayOrderStaticsServiceImpl extends ServiceImpl<JobDayOrderStaticsMapper, JobDayOrderStaticsEntity>
    implements JobDayOrderStaticsService {

    /**
     * 计算统计信息。
     * 该方法用于统计指定时间范围内的订单、退款和其他相关指标的数量和金额。
     *
     * @param start 统计开始时间
     * @param end 统计结束时间
     * @return 包含各种统计指标的映射
     */
    @Override
    public Map<String, Double> countStatics(Date start, Date end) {
        // 格式化日期以用于数据库查询
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 初始化完成订单的统计参数
        Map<String,String> completeMap = new HashMap<>();
        completeMap.put("orderFinishedStartTime",sdf.format(start));
        completeMap.put("orderFinishedEndTime",sdf.format(end));
        completeMap.put("orderStatus","6");
        // 查询完成的订单数量和金额
        Map<String, String> completeOrder = this.baseMapper.countOrderBase(completeMap);

        // 初始化取消订单的统计参数
        Map<String,String> cancelMap = new HashMap<>();
        cancelMap.put("orderCancelStartDate",sdf.format(start));
        cancelMap.put("orderCancelEndDate",sdf.format(end));
        cancelMap.put("orderStatus","7");
        // 查询取消的订单数量和金额
        Map<String, String> cancelOrder = this.baseMapper.countOrderBase(cancelMap);

        // 初始化所有订单的统计参数
        Map<String,String> totalMap = new HashMap<>();
        totalMap.put("orderCreateStartTime",sdf.format(start));
        totalMap.put("orderCreateEndTime",sdf.format(end));
        // 查询所有订单的数量和金额
        Map<String, String> totalOrder = this.baseMapper.countOrderBase(totalMap);

        // 初始化退款的统计参数
        Map<String,String> returnMap = new HashMap<>();
        returnMap.put("orderReturnStartTime",sdf.format(start));
        returnMap.put("orderReturnEndTime",sdf.format(end));
        returnMap.put("returnState","5");
        // 查询完成的退款数量和金额
        Map<String, String> returnOrder = this.baseMapper.countAfterSaleOrder(returnMap);

        // 初始化最终的统计结果映射
        Map<String,Double> paraMap = new HashMap<>();
        // 统计完成订单的金额和数量
        paraMap.put("totalRefundFee", Double.parseDouble(returnOrder.get("returnCash").toString()));
        paraMap.put("totalRefundTrade",Double.parseDouble(returnOrder.get("completeTrade").toString()));
        // 统计所有订单的金额和数量
        paraMap.put("totalPaymentAmount",Double.parseDouble(totalOrder.get("orderPaymentAmount").toString()));
        paraMap.put("totalOrderTrade",Double.parseDouble(totalOrder.get("completeTrade").toString()));
        // 统计完成订单的金额和数量
        paraMap.put("completeTrade",Double.parseDouble(completeOrder.get("completeTrade").toString()));
        paraMap.put("completeFee",Double.parseDouble(completeOrder.get("orderPaymentAmount").toString()));
        // 统计取消订单的金额和数量
        paraMap.put("cancleTrade",Double.parseDouble(cancelOrder.get("completeTrade").toString()));
        paraMap.put("cancleFee",Double.parseDouble(cancelOrder.get("orderPaymentAmount").toString()));

        return paraMap;
    }

}




