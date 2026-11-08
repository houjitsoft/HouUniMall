package com.houjiang.job.job;

import com.houjiang.job.dto.ForSettleShopInfoDTO;
import com.houjiang.job.mapper.ForSettleShopMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @ClassName SettleAccountsJob
 * @Description 结算单
 * @Author Amos
 * @Date 2023/5/17 16:05
 * @Version 1.0
 * @author Amos
 */
public class SettleWebPosOrderJob implements BaseJob {

    /**
     * 结算账户服务接口，用于处理结算相关业务逻辑。
     */
    @Autowired
    private SettleAccountsServices settleAccountsServices;

    /**
     * 用于结算的店铺信息映射器接口，用于查询需要结算的店铺信息。
     */
    @Autowired
    private ForSettleShopMapper forSettleShopMapper;

    /**
     * 执行作业的具体逻辑。该方法从数据库中获取需要结算的店铺信息，
     * 然后调用结算服务为每个店铺进行结算。
     *
     * @param context 作业执行上下文，包含作业执行所需的环境信息。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 打印日志，标记作业开始执行
        System.out.println("开始执行SettleAccountsJob");

        // 从数据库中查询需要结算的店铺信息
        List<ForSettleShopInfoDTO> settleShopInfos = forSettleShopMapper.getGateShopInfo();

        // 遍历所有店铺信息，对每个店铺调用结算服务
        for (ForSettleShopInfoDTO settleShopInfo : settleShopInfos) {
            // 单个进行结算
            settleAccountsServices.settleWebPosOrder(settleShopInfo);
        }
    }

}
