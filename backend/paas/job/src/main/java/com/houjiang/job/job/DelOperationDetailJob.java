package com.houjiang.job.job;

import com.houjiang.job.mapper.OperationDetailMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName DelOperationDetailJob
 * @Description 删除用户日志
 * @Author Amos
 * @Date 2023/5/17 14:27
 * @Version 1.0
 * @author Amos
 */
public class DelOperationDetailJob implements BaseJob {

    /**
     * 自动注入OperationDetailMapper接口的实现，用于操作细节的数据库操作。
     * 通过@Autowired注解，Spring框架会自动将OperationDetailMapper的实现实例化并注入到当前字段中。
     * 这样就可以在当前类中直接使用operationDetailMapper对象进行数据库的查询、插入、更新等操作。
     */
    @Autowired
    private OperationDetailMapper operationDetailMapper;

    /**
     * 定时清空用户操作日志表udb_ucenter_log_action
     *
     * @param jobExecutionContext 作业执行上下文，包含作业执行所需的信息和状态。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 调用operationDetailMapper的delete方法，执行删除操作。此行为作业的具体逻辑。
        operationDetailMapper.delete();
    }
}
