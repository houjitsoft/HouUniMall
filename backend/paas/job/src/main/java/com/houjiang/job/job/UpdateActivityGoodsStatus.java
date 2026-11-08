package com.houjiang.job.job;


import com.houjiang.job.service.UpdateActivityStatusService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Amos
 * 限时活动
 */
public class UpdateActivityGoodsStatus implements BaseJob {
    /**
     * 更新活动商品状态的作业执行类。
     * 该类负责在特定时间点触发各种活动状态的更新，包括限时折扣、拼团、满减赠折和秒杀活动。
     */
    private static final Logger logger = LoggerFactory.getLogger(UpdateActivityGoodsStatus.class);

    @Autowired
    UpdateActivityStatusService updateActivityStatusService;

    /**
     * 执行作业的核心方法。
     *
     * @param context 作业执行的上下文，包含作业的相关信息和状态。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 日志记录作业开始
        // 限时折扣过期状态更新
        logger.info("限时折扣过期状态更新");

        // 更新限时折扣商品状态
        updateActivityStatusService.updatePromotionGoods();
        // 更新限时折扣基础信息
        updateActivityStatusService.updatePromotionDiscountBase();
        // 更新限时折扣商品状态
        updateActivityStatusService.updatePromotionDiscountGoods();

        // 日志记录预热状态更新
        // 限时折扣预热状态更新
        logger.info("限时折扣预热状态更新");

        // 更新限时折扣预热状态
        updateActivityStatusService.updatePromotionDiscountBase2();
        // 更新限时折扣商品预热状态
        updateActivityStatusService.updatePromotionDiscountGoods2();

        // 日志记录开始状态更新
        // 限时折扣开始状态更新
        logger.info("限时折扣开始状态更新");

        // 更新限时折扣开始状态
        updateActivityStatusService.updatePromotionDiscountBase3();
        // 更新限时折扣商品开始状态
        updateActivityStatusService.updatePromotionDiscountGoods3();

        // 日志记录拼团状态更新
        // 拼团过期状态更新
        logger.info("拼团状态更新");

        // 更新拼团标记状态
        updateActivityStatusService.updatePintuanMarkStatus();
        // 更新拼团商品状态
        updateActivityStatusService.updatePintuanGoods();
        // 更新拼团状态
        updateActivityStatusService.updatePintuanStatus();
        // 更新拼团配置状态
        updateActivityStatusService.updatePintuanConfig();

        // 日志记录满减赠折过期状态更新
        // 满减赠折过期状态更新
        logger.info("满减赠折过期状态更新");

        // 更新满减赠折商品状态
        updateActivityStatusService.updateManjianzengzheGoods();
        // 更新满减赠折状态
        updateActivityStatusService.updateManjianzengzheStatus();

        // 日志记录秒杀过期状态更新
        // 秒杀过期状态更新
        logger.info("秒杀过期状态更新");

        // 更新秒杀商品状态
        updateActivityStatusService.updateSkillGoodsStatus();
        // 更新秒杀商品信息
        updateActivityStatusService.updateSkillGoods();
        // 更新商品状态
        updateActivityStatusService.updateGoodsStatus();

        // 日志记录秒杀开始状态更新
        // 秒杀开始状态更新
        logger.info("秒杀开始状态更新");

        // 更新秒杀商品开始状态
        updateActivityStatusService.updateSkillGoods2();
        // 更新秒杀商品状态
        updateActivityStatusService.updateSkillGoodsStatus2();
        // 更新商品开始状态
        updateActivityStatusService.updateGoodsStatus2();

        // 更新秒杀商品库存状态，根据库存情况在商品和秒杀商品之间切换
        // 更改当秒杀库存被抢空后，回到普通商品，秒杀库存退回时则改回秒杀商品
        updateActivityStatusService.updateSkillGoods3();

        // 日志记录拼团待开启状态更新
        // 拼团待开启状态更新
        logger.info("拼团待开启状态更新");

        // 更新拼团待开启状态
        updateActivityStatusService.updatePintuanStatus2();

        // 日志记录满减赠折待开始状态更新
        // 满减赠折过期状态更新
        logger.info("满减赠折待开始状态更新");

        // 更新满减赠折待开始状态
        updateActivityStatusService.updateManjianzengzheStatus2();

        // 日志记录满减赠折开始状态更新
        // 满减赠折开始状态更新
        logger.info("满减赠折开始状态更新");

        // 更新满减赠折开始状态
        updateActivityStatusService.updateManjianzengzheStatus2();
    }

}
