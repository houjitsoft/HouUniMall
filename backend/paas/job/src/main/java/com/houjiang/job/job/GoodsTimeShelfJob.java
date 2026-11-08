package com.houjiang.job.job;

import com.houjiang.commons.util.DateUtils;
import com.houjiang.job.feign.SupplierServiceClient;
import com.houjiang.job.mapper.MerchantGoodsCommonJobMapper;
import jakarta.annotation.Resource;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsTimeShelfJob
 * @Description 商品定时上架
 * @Author Amos
 * @Date 2023/5/17 11:25
 * @Version 1.0
 * @author Amos
 */
public class GoodsTimeShelfJob implements BaseJob {

    /**
     * 自动注入MerchantGoodsCommonJobMapper接口的实现，用于操作商家商品的通用任务数据。
     * 该注解使得系统能够在运行时自动为merchantGoodsCommonJobMapper变量赋值，
     * 赋值的对象是Spring框架根据MerchantGoodsCommonJobMapper接口定义的bean。
     * 通过这个变量，可以执行对商家商品通用任务相关数据的增删改查操作。
     */
    @Autowired
    private MerchantGoodsCommonJobMapper merchantGoodsCommonJobMapper;

    @Resource
    private SupplierServiceClient supplierServiceClient;

    /**
     * 执行商品定时上架任务。
     * 该方法旨在检查是否有商品需要在当前时间上架，并对符合条件的商品执行上架操作。
     * 它首先从数据库查询所有定时上架的商品，然后判断每个商品的上架时间是否已到，如果已到，则更新商品状态为上架。
     *
     * @param context 作业执行上下文，提供作业执行所需的环境信息和配置。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // 从数据库查询所有定时上架的商品信息
            //查询出所有定时发布的商品
            List<Map<String, Object>> list = merchantGoodsCommonJobMapper.queryGoodsTimeShelf();
            // 检查查询结果是否存在
            if (null != list && !list.isEmpty()) {
                // 遍历每个商品的详细信息
                for (Map<String, Object> map : list) {
                    // 检查商品的定时上架时间是否已到
                    if (null != map.get("commonSellTime")) {
                        LocalDateTime commonSellTime = (LocalDateTime) map.get("commonSellTime");
                        Boolean orverTimeStart = DateUtils.compareDate2(commonSellTime);
                        // 如果定时上架时间未到，则跳过该商品
                        //判断当前商品发布的时间是否大于当前时间
                        if (!orverTimeStart) {
                            // 获取商品的唯一标识ID
                            Integer commonId = ((Long) map.get("commonId")).intValue();
                            // 更新商品状态为上架
                            merchantGoodsCommonJobMapper.updateGoodsPutaway(commonId);
                            // 更新商品的基础信息
                            merchantGoodsCommonJobMapper.updateGoodsBase(commonId);
                        }
                    }
                }
            }
            supplierServiceClient.updateSupplierGoodsTimeShelfJob();
        } catch (Exception e) {
            // 打印异常堆栈跟踪信息
            e.printStackTrace();
        }
    }

}
