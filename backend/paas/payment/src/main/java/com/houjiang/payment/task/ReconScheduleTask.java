package com.houjiang.payment.task;

import com.houjiang.payment.service.api.ReconService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 自研对账定时调度任务（完整版）。
 *
 * 自有平台对账主线由本类驱动，不依赖通企付。
 * 执行流程：拉取账单 → 差异核对 → 自动生成月报。
 *
 * 计划任务：
 *  - 每日 02:00 拉取昨日 4 大渠道账单并执行差异核对
 *  - 每月 1 号 03:00 生成上月对账报表
 *
 * 开关控制：通过 nacos 配置 task.recon.enabled=true 开启。
 *
 * @author Amos
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task.recon.enabled", havingValue = "true", matchIfMissing = false)
public class ReconScheduleTask {

    private final ReconService reconService;

    private static final String[] CHANNELS = {"WX", "ALI", "UNION", "YSF"};

    @Value("${task.recon.enabled:false}")
    private boolean enabled;

    /**
     * 每日 02:00 拉取昨日 4 渠道账单并执行差异核对。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyPullAndCheck() {
        if (!enabled) {
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        log.info("[ReconTask] 开始每日对账 日期={}", yesterday);

        int totalDiff = 0;
        for (String ch : CHANNELS) {
            try {
                // 1. 拉取账单
                Map<String, Object> req = new HashMap<>();
                req.put("channel", ch);
                req.put("billDate", yesterday);
                req.put("billType", "TRADE");
                reconService.pullChannelBill(req);

                // 2. 差异核对
                int diffCount = reconService.diffCheck(yesterday, ch);
                totalDiff += diffCount;
                log.info("[ReconTask] 渠道 {} 日期 {} 拉取+核对完成 差异数={}", ch, yesterday, diffCount);
            } catch (Exception e) {
                log.error("[ReconTask] 渠道 {} 拉单+核对失败 date={}", ch, yesterday, e);
            }
        }
        log.info("[ReconTask] 每日对账完成 日期={} 总差异数={}", yesterday, totalDiff);
    }

    /**
     * 每月 1 号 03:00 生成上月报表。
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void monthlyReportGen() {
        if (!enabled) {
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        String lastMonth = new SimpleDateFormat("yyyy-MM").format(cal.getTime());
        log.info("[ReconTask] 开始生成月报 月份={}", lastMonth);

        for (String ch : CHANNELS) {
            try {
                Map<String, Object> req = new HashMap<>();
                req.put("channel", ch);
                req.put("reportMonth", lastMonth);
                reconService.reportGen(req);
                log.info("[ReconTask] monthlyReportGen 渠道 {} 月份 {} 已完成", ch, lastMonth);
            } catch (Exception e) {
                log.error("[ReconTask] 渠道 {} 月报失败 month={}", ch, lastMonth, e);
            }
        }
    }
}
