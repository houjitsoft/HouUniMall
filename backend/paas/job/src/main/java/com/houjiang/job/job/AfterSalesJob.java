package com.houjiang.job.job;


import com.houjiang.job.feign.OrderServiceClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 售后超时未处理定时任务
 * @author Amos
 */
public class AfterSalesJob implements BaseJob {

    @Autowired
    OrderServiceClient orderService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        //退货商家超时未处理确认收货
        orderService.updateTimeOutReturnGoods();

        //商家拒绝退款/仅退款 买家超时未处理
        orderService.updateTimeOutBuyReturn();

        //商家拒绝退货 买家超时未处理
        orderService.updatebuyTimeOutReturn();

        //申请平台介入卖家超时未处理
        orderService.updateTimeOutReturnplatform();

        //商家同意退货 买家未在有效期内填写物流信息
        orderService.updatebuyTimeOutReturnGoods();


        //平台超时未处理
        orderService.updateplatform();

    }
}
