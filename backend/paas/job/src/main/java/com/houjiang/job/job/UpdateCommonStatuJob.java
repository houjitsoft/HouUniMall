package com.houjiang.job.job;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.service.UpdateCommonStatuJobService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Amos
 * 定时下架库存为0的商品
 */
public class UpdateCommonStatuJob implements BaseJob {
    /**
     * 定义一个日志器，用于记录UpdateCommonStatuJob类的操作日志。
     */
    private static final Logger logger = LoggerFactory.getLogger(UpdateCommonStatuJob.class);

    /**
     * 通过@Autowired注解，自动注入UpdateCommonStatuJobService实例，
     * 以便在任务执行中调用相关业务方法。
     */
    @Autowired
    private UpdateCommonStatuJobService updateCommonStatuJobService;

    /**
     * 实现Job接口的execute方法，该方法定义了定时任务的具体执行逻辑。
     *
     * @param context 作业执行上下文，包含作业执行时的环境信息。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 调用updateCommonStatuJobService的queryGoodsIdByState方法，查询指定状态的商品ID列表。
        ResponseResult resp = updateCommonStatuJobService.queryGoodsIdByState();
    }

}
