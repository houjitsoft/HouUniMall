package com.houjiang.job.job;


import com.houjiang.job.feign.OrderServiceClient;
import com.houjiang.job.service.UpdateOrderStatusService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 退款/仅退款订单超24小时商家未处理,自动同意
 * @author Amos
 */
public class RefundOrderHandleJob implements BaseJob {
    private static final Logger logger = LoggerFactory.getLogger(RefundOrderHandleJob.class);

    @Autowired
    private UpdateOrderStatusService updateOrderStatusService;

    @Autowired
    OrderServiceClient orderService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        // 查询超过48小时商家未处理的订单
//        updateOrderStatusService.refundOrderHandleJob();
        //退款/仅退款 商家超时未处理
        orderService.updateTimeOutReturn();

    }

}
