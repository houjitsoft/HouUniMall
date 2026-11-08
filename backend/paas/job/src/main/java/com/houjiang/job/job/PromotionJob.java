package com.houjiang.job.job;

import com.houjiang.job.dto.PromotionDTO;
import com.houjiang.job.mapper.PromotionMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @ClassName PromotionJob
 * @Description 订单超过售后期满足条件会员晋升
 * @Author Amos
 * @Date 2023/5/17 9:52
 * @Version 1.0
 * @author Amos
 */
public class PromotionJob implements BaseJob {

    /**
     * 自动注入PromotionMapper，用于执行与促销相关的数据库操作。
     */
    @Autowired
    private PromotionMapper promotionMapper;

    /**
     * 执行作业，更新用户的等级。
     *
     * @param jobExecutionContext 作业执行上下文，提供作业执行所需的上下文信息。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 获取所有促销订单信息
        // 查询会员等级删除了的用户
        List<PromotionDTO> promotionList = promotionMapper.getPromotionByOrder();
        // 遍历促销订单列表，更新每个订单相关的用户积分和等级
        for (PromotionDTO promotion : promotionList) {
            // 根据订单用户ID查询用户积分
            Integer integral = promotionMapper.getUserIntegralByOrder(promotion.getUserId());
            // 计算用户的总积分，包括订单积分和现有积分的累加
            Integer userIntegral = (promotion.getUserIntegral() == null ? 0 : promotion.getUserIntegral()) +
                    (integral == null ? 0 : integral);
            // 根据总积分查询对应的用户等级ID
            Integer gradeId = promotionMapper.getGradeIdByUserIntegral(userIntegral);
            // 更新用户的等级ID
            promotionMapper.updateGradeId(gradeId, promotion.getUserId());
        }
    }

}