package com.houjiang.payment.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.channel.CompensateListRequest;
import com.houjiang.payment.dto.channel.CompensateRegisterRequest;
import com.houjiang.payment.entity.PayChannelCompensateEntity;

/**
 * 统一补偿查询服务
 * register：掉单时注册补偿任务
 * executeDue：定时任务扫描到期任务并驱动查询
 * @author Amos
 */
public interface CompensateService {

    /** 注册补偿任务 */
    /**
     * @param req 请求
     * @return 处理结果
     */
    String register(CompensateRegisterRequest req);

    /** 立即执行某一条补偿任务（供定时任务驱动） */
    /**
     * @param id 主键
     * @return 处理结果
     */
    boolean executeOne(Long id);

    /** 批量执行到期任务（返回执行条数） */
    /**
     * @param limit limit
     * @return 处理结果
     */
    int executeDue(int limit);

    /** 补偿任务分页 */
    /**
     * @param req 请求
     * @return 处理结果
     */
    IPage<PayChannelCompensateEntity> page(CompensateListRequest req);

    /** 任务详情 */
    /**
     * @param taskNo task编号
     * @return 处理结果
     */
    PayChannelCompensateEntity detail(String taskNo);

    /** 放弃任务 */
    /**
     * @param taskNo task编号
     * @param reason reason
     * @return 处理结果
     */
    boolean giveUp(String taskNo, String reason);
}
