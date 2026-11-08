package com.houjiang.job.job;

import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.vo.activity.EffectiveActivityVO;
import com.houjiang.job.mapper.SpinwinActivityMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @ClassName UpdateSpinwinActivityStatusJob
 * @Description
 * @Author Amos
 * @Date 2023/9/5 13:57
 * @Version 1.0
 * @author Amos
 */
public class UpdateSpinwinActivityStatusJob implements BaseJob {

    /**
     * 日志对象，用于记录执行过程中的信息。
     */
    private static final Logger LOG = LoggerFactory.getLogger(UpdateSpinwinActivityStatusJob.class);

    /**
     * 大转盘活动Mapper对象，用于数据库操作。
     */
    @Autowired
    private SpinwinActivityMapper spinwinActivityMapper;

    /**
     * 执行作业的主方法。
     *
     * @param context 作业执行的上下文，包含作业的详细信息。
     * @throws JobExecutionException 如果作业执行过程中出现异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 记录作业开始执行的信息
        LOG.info("更新大转盘活动状态");
        // 查询已过期的活动ID列表
        // 查询过期活动
        List<Integer> activityIdList = spinwinActivityMapper.selectExpireActivity();

        // 如果存在过期活动，则更新相关活动状态
        if (!CollectionUtils.isEmpty(activityIdList)) {
            spinwinActivityMapper.updateLuckyDraw(activityIdList);
            spinwinActivityMapper.updateActivityStatus(activityIdList);
        }

        // 查询当前有效的活动信息
        //查询可用活动
        List<EffectiveActivityVO> activity = spinwinActivityMapper.selectEffectiveActivity();
        // 遍历有效活动列表，更新每日抽奖次数
        if (!CollectionUtils.isEmpty(activity)) {
            for (EffectiveActivityVO effectiveActivityVO : activity) {
                // 修改每日抽奖次数  一天只执行一次
                String date = DateUtils.getDate();
                if(!RedisUtil.exists("SPINWIN_ACTIVITY_"+date+"_"+effectiveActivityVO.getActivityId())){
                    spinwinActivityMapper.updateDailyLottery(effectiveActivityVO);
                    RedisUtil.set("SPINWIN_ACTIVITY_"+date+"_"+effectiveActivityVO.getActivityId(),effectiveActivityVO.getActivityId().toString(),RedisUtil.EXRP_TWO_DAY);
                }
            }
        }
    }

}
