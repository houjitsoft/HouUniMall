package com.houjiang.job.job;


import com.houjiang.commons.dto.goods.AppSeckillGoodsVO;
import com.houjiang.job.mapper.SeckillGoodsMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

//限时秒杀库存为零时下架商品

/**
 * @author Amos
 */
public class UpdateSeckillNumStartJob implements BaseJob {
    /**
     * 日志记录器，用于记录作业执行过程中的信息。
     */
    private static final Logger logger = LoggerFactory.getLogger(UpdateActivityGoodsStatus.class);
    /**
     * 注入秒杀商品Mapper，用于数据库操作。
     */
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 执行定时任务，更新秒杀活动商品的状态。
     *
     * @param jobExecutionContext 作业执行上下文，包含作业相关的所有信息。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 查询所有秒杀商品信息
        // 限时秒杀
        List<AppSeckillGoodsVO> seckillGoodsResults = seckillGoodsMapper.selectSeckillNum();
        // 遍历查询到的秒杀商品信息
        if (seckillGoodsResults != null && seckillGoodsResults.size() > 0) {
            for (AppSeckillGoodsVO seckillGoodsResult : seckillGoodsResults) {
                // 获取当前秒杀商品的ID
                Integer seckillId = seckillGoodsResult.getSeckillId();
                // 获取当前秒杀商品的通用ID
                Integer commonId = seckillGoodsResult.getCommonId();
                // 获取当前秒杀商品的商品ID
                Integer goodsId = seckillGoodsResult.getGoodsId();
                // 查询当前秒杀活动下是否还有其他商品存在库存
                // 查询该活动是否还有其他秒杀库存不为零的商品
                List<AppSeckillGoodsVO> seckillGoods = seckillGoodsMapper.selectSeckillGoods(seckillId);
                // 如果当前秒杀活动下还有其他商品存在库存
                if (seckillGoods != null && seckillGoods.size() > 0) {
                    // 更新当前商品的状态，标记为秒杀结束
                    // 只更新该商品状态
                    seckillGoodsMapper.updateSkillGoods1(goodsId);
                } else {
                    // 如果当前秒杀活动下没有其他商品存在库存
                    // 更新当前秒杀商品的通用ID对应的商品状态，标记为秒杀结束
                    seckillGoodsMapper.updateSkillGoodsStatus(commonId);
                    // 更新当前秒杀活动的状态
                    seckillGoodsMapper.updateSkillGoods(seckillId);
                    // 更新所有参与该秒杀活动的商品状态
                    seckillGoodsMapper.updateGoodsStatus(seckillId);
                }
            }
        }
    }

}
