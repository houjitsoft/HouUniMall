package com.houjiang.job.job;


import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.job.service.UpdateRecordStatusService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * 确认收货超过 30 天默认好评（兼容历史 14 天文案）
 * <p>
 * XXL-JOB：可直接用中文 JobHandler「确认收货超过30天默认默认好评」，
 * 或通过 quartzJobHandler + 参数 com.houjiang.job.job.AcquiescenceJob。
 * @author Amos
 */
@Slf4j
@Component
public class AcquiescenceJob implements BaseJob {

    @Autowired
    UpdateRecordStatusService updateRecordStatusService;

    /**
     * 供 xxl-job 管理台按中文 Handler 名触发（与线上任务配置一致）
     */
    @XxlJob("确认收货超过30天默认默认好评")
    public void executeByXxlJob() throws Exception {
        XxlJobHelper.log("XXL-JOB 任务开始执行...确认收货超过30天默认默认好评");
        try {
            String summary = doExecute();
            XxlJobHelper.log(summary);
            XxlJobHelper.handleSuccess(summary);
        } catch (Exception e) {
            log.error("默认好评任务失败", e);
            XxlJobHelper.log("执行失败: " + e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
            throw e;
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            doExecute();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    /**
     * 扫描确认收货超 30 天且未评价订单，写入默认好评并回写评价状态。
     *
     * @return 执行摘要，便于调度日志排查
     */
    String doExecute() {
        List<AcquiescenceVO> list = updateRecordStatusService.queryNotEvaluatedOrder();
        if (list == null) {
            list = Collections.emptyList();
        }
        int candidate = list.size();
        int success = 0;
        int skipNoUser = 0;
        int skipNoGoods = 0;
        int skipNoFinishTime = 0;
        int insertFail = 0;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyy-MM-dd");

        for (AcquiescenceVO order : list) {
            if (order == null || !StringUtils.hasText(order.getOrderId())) {
                continue;
            }
            try {
                AcquiescenceVO userInfo = updateRecordStatusService.queryUserInfo(order.getBuyerUserId());
                if (userInfo == null || !StringUtils.hasText(userInfo.getUserName())) {
                    skipNoUser++;
                    log.warn("默认好评跳过：无用户信息 orderId={}, buyerUserId={}",
                            order.getOrderId(), order.getBuyerUserId());
                    continue;
                }
                order.setUserName(userInfo.getUserName());
                order.setUserAvatar(userInfo.getUserAvatar() == null ? " " : userInfo.getUserAvatar());

                List<AcquiescenceVO> goodsInfo = updateRecordStatusService.queryGoodsInfo(order.getOrderId());
                if (CollectionUtils.isEmpty(goodsInfo)) {
                    skipNoGoods++;
                    log.warn("默认好评跳过：无商品明细 orderId={}", order.getOrderId());
                    continue;
                }
                order.setGoodsId(goodsInfo.get(0).getGoodsId());
                order.setCommonId(goodsInfo.get(0).getCommonId());
                order.setGoodsName(goodsInfo.get(0).getGoodsName());
                order.setGoodsSpec(goodsInfo.get(0).getGoodsSpec());

                order.setScores(5);
                order.setResult("good");
                order.setContent("此用户未填写评价内容");
                order.setIsanonymous(0);
                order.setStatus(1);
                order.setEvaluationFrom(5);

                String shouHuoshijian = updateRecordStatusService.queryShouHuoshijian(order.getOrderId());
                if (!StringUtils.hasText(shouHuoshijian)
                        && StringUtils.hasText(order.getOrderReceiverDate())) {
                    shouHuoshijian = order.getOrderReceiverDate();
                }
                if (!StringUtils.hasText(shouHuoshijian)) {
                    skipNoFinishTime++;
                    log.warn("默认好评跳过：无完成/签收时间 orderId={}", order.getOrderId());
                    continue;
                }

                String finishRaw = shouHuoshijian.trim();
                if (finishRaw.length() == 10) {
                    finishRaw = finishRaw + " 00:00:00";
                } else if (finishRaw.length() > 19) {
                    finishRaw = finishRaw.substring(0, 19);
                }
                Date parse = dateformat.parse(finishRaw);
                cal.setTime(parse);
                cal.add(Calendar.DAY_OF_MONTH, 6);
                Date date = cal.getTime();
                String format1 = dateformat1.format(date);
                Date date1 = DateUtils.randomDate(format1 + " 06:00:00", format1 + " 24:00:00");
                order.setCreateTime(dateformat.format(date1));

                Integer code = updateRecordStatusService.insertEvaluated(order);
                // commons SUCCESS=1；兼容部分环境返回 200
                if (code != null && (code == 1 || code == 200)) {
                    updateRecordStatusService.updateOrderEvaluated(order.getOrderId());
                    success++;
                } else {
                    insertFail++;
                    log.warn("默认好评写入失败 orderId={}, code={}", order.getOrderId(), code);
                }
            } catch (Exception e) {
                insertFail++;
                log.error("默认好评处理异常 orderId={}", order.getOrderId(), e);
            }
        }

        String summary = String.format(
                "确认收货超30天默认好评完成: 候选=%d, 成功=%d, 跳过(无用户=%d,无商品=%d,无时间=%d), 失败=%d",
                candidate, success, skipNoUser, skipNoGoods, skipNoFinishTime, insertFail);
        log.info(summary);
        return summary;
    }

}
