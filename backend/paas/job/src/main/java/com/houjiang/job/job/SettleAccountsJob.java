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
/**
 * @RestController
 * @RequestMapping("job")
 */
public class SettleAccountsJob implements BaseJob {

    /**
     * 结算账户服务接口，用于处理账户结算业务逻辑。
     */
    @Autowired
    private SettleAccountsServices settleAccountsServices;

    /**
     * 用于结算店铺信息查询的Mapper接口。
     */
    @Autowired
    private ForSettleShopMapper forSettleShopMapper;

    /**
     * 执行作业的具体逻辑。从数据库中获取需要结算的店铺信息，并对每个店铺进行结算操作。
     *
     * @param context 作业执行上下文，提供作业执行所需的环境信息。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    /** @PostMapping("/test") */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 打印开始执行作业的信息
        System.out.println("开始执行SettleAccountsJob");

        // 从数据库中查询需要结算的店铺信息
        List<ForSettleShopInfoDTO> settleShopInfos = forSettleShopMapper.getForSettleShopInfo();

        // 遍历查询到的店铺信息，对每个店铺进行结算操作
        for (ForSettleShopInfoDTO settleShopInfo : settleShopInfos) {
            // 单个进行结算
            settleAccountsServices.settleAccounts(settleShopInfo);
        }

        //供应商结算
        List<ForSettleShopInfoDTO> settleSupplierShopInfos = forSettleShopMapper.getForSettleSupplierShopInfo();
        for (ForSettleShopInfoDTO settleShopInfo : settleSupplierShopInfos) {
            //单个进行结算
            settleAccountsServices.settleSupplierAccounts(settleShopInfo);
        }
    }

}
