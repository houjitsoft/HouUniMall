//package com.houjiang.job.job;
//
//
//import com.houjiang.job.dto.ForSettleShopInfoDTO;
//import com.houjiang.job.service.ForSettleShopService;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.List;
//
//
///**
// * @author Amos
// * 经销订单供应商结算单
// */
//public class SettleSupplyAccountsJob implements BaseJob {
//
//    /**
//     * 结算账户服务，用于处理账户结算相关业务。
//     */
//    @Autowired
//    SettleAccountsServices settleAccountsServices;
//
//    /**
//     * 结算店铺服务，用于获取需要结算的店铺信息。
//     */
//    @Autowired
//    ForSettleShopService forSettleShopService;
//
//    /**
//     * 执行作业的主体方法。
//     *
//     * @param context 作业执行的上下文，包含作业的详细信息和执行环境。
//     * @throws JobExecutionException 如果作业执行过程中发生异常。
//     */
//    @Override
//    public void execute(JobExecutionContext context)
//            throws JobExecutionException {
//        // 打印开始执行作业的提示信息
//        System.out.println("开始执行SettleAccountsJob");
//        // 获取需要结算的店铺信息
//        List<ForSettleShopInfoDTO> settleShopInfos = forSettleShopService.getForSettleSupplyShopInfo();
//        // 遍历所有需要结算的店铺，进行结算操作
//        for (ForSettleShopInfoDTO settleShopInfo : settleShopInfos) {
//            // 单个进行结算
//            settleAccountsServices.settleSupplyAccounts(settleShopInfo);
//        }
//    }
//
//}