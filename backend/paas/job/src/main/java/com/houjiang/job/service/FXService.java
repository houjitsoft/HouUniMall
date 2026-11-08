package com.houjiang.job.service;

/**
 * @ClassName FXService
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:19
 * @Version 1.0
 * @author Amos
 */

public interface FXService {
    /**
     * 调用该方法来执行与VK订单相关的佣金计算任务。
     * 该方法不接受任何参数，也不返回任何结果。
     * 主要用于在特定的时间点或条件下触发对VK平台订单的佣金计算和处理。
     */
    void vkOrderCommission();

}
