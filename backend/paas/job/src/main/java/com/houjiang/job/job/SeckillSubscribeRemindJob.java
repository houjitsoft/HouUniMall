package com.houjiang.job.job;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.job.feign.BaseServiceClient;
import com.houjiang.job.mapper.SeckillSubscribeRemindMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 限时秒杀「开抢前预约」提醒定时任务。
 *
 * <p>扫描 order-seckill 插件预约表中「即将开抢且未提醒」的预约，逐条通过 base 服务
 * {@code sendMessageTemplate} 发送站内信 + 短信（模板 id = {@link CommonType.messageTemplate#CODE_SECKILL_REMIND}），
 * 发送后回写 remind_status，避免重复提醒。
 *
 * <p>注册方式：hou_job_platform 增加一行，invoke_target = 本类全限定名，cron 建议每分钟一次。
 *
 * @author Amos
 */
public class SeckillSubscribeRemindJob implements BaseJob {

    private static final Logger logger = LoggerFactory.getLogger(SeckillSubscribeRemindJob.class);

    /** 开抢前多少分钟开始提醒 */
    private static final int ADVANCE_MINUTES = 5;

    /** 单次最多处理条数，防止积压时一次跑太多 */
    private static final int BATCH_LIMIT = 500;

    /** 秒杀开抢提醒模板 id */
    private static final int TEMPLATE_ID = CommonType.messageTemplate.CODE_SECKILL_REMIND.getCode();

    @Autowired
    private SeckillSubscribeRemindMapper seckillSubscribeRemindMapper;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        List<Map<String, Object>> dueList;
        try {
            dueList = seckillSubscribeRemindMapper.selectDueSubscribes(ADVANCE_MINUTES, BATCH_LIMIT);
        } catch (Exception e) {
            logger.error("秒杀开抢提醒：查询待提醒预约失败", e);
            return;
        }
        if (dueList == null || dueList.isEmpty()) {
            return;
        }

        int success = 0;
        int failed = 0;
        for (Map<String, Object> row : dueList) {
            Object subscribeIdObj = row.get("subscribeId");
            if (subscribeIdObj == null) {
                continue;
            }
            int subscribeId = ((Number) subscribeIdObj).intValue();
            try {
                Map<String, Object> param = new HashMap<>();
                param.put("templateId", TEMPLATE_ID);
                param.put("userId", row.get("userId"));
                // 预约时已快照手机号，直接带上，base 侧无需再查用户服务
                if (row.get("userMobile") != null) {
                    param.put("mobile", row.get("userMobile"));
                }
                // 站内信/短信内容占位符：{1}=商品名 {2}=开抢时间
                List<String> content = Arrays.asList(
                        String.valueOf(row.getOrDefault("goodsName", "")),
                        String.valueOf(row.getOrDefault("seckillStartTime", "")));
                param.put("list", content);

                baseServiceClient.sendMessageTemplate(param);
                seckillSubscribeRemindMapper.updateRemindStatus(subscribeId, 1);
                success++;
            } catch (Exception e) {
                failed++;
                logger.error("秒杀开抢提醒发送失败 subscribeId={}", subscribeId, e);
                try {
                    seckillSubscribeRemindMapper.updateRemindStatus(subscribeId, 2);
                } catch (Exception ignore) {
                    logger.error("秒杀开抢提醒回写失败状态异常 subscribeId={}", subscribeId, ignore);
                }
            }
        }
        logger.info("秒杀开抢提醒任务完成：成功 {} 条，失败 {} 条", success, failed);
    }
}
