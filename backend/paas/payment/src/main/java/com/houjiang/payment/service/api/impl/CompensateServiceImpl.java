package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.payment.dto.channel.CompensateListRequest;
import com.houjiang.payment.dto.channel.CompensateRegisterRequest;
import com.houjiang.payment.entity.PayChannelCompensateEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayChannelCompensateMapper;
import com.houjiang.payment.mapper.PayCashierOrderMapper;
import com.houjiang.payment.service.cashier.CashierBizNotifyService;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.constant.CashierOrderStatusEnum;
import com.houjiang.payment.service.api.CompensateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 统一补偿查询服务实现
 *
 * 任务状态：1-待执行 2-执行中 3-成功 4-失败 5-放弃
 * 执行流程：注册 → executeDue 批量扫描到期 → executeOne 查询第三方接口（此处桩化）
 *           → 成功置 3；未知则递增 queryCount 并计算下一次执行时间
 *           → queryCount > maxQuery 置 4 最终失败
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensateServiceImpl implements CompensateService {

    private final PayChannelCompensateMapper compensateMapper;
    private final PayCashierOrderMapper cashierOrderMapper;
    private final CashierBizNotifyService bizNotifyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String register(CompensateRegisterRequest req) {
        log.info("[compensate/register] bizNo={}, bizType={}, channelCode={}", req.getBizNo(), req.getBizType(), req.getChannelCode());
        // 幂等：同一业务单 + 渠道 若已存在未结单任务则直接返回
        PayChannelCompensateEntity exist = compensateMapper.selectOne(new LambdaQueryWrapper<PayChannelCompensateEntity>()
                .eq(PayChannelCompensateEntity::getBizNo, req.getBizNo())
                .eq(PayChannelCompensateEntity::getChannelCode, req.getChannelCode())
                .in(PayChannelCompensateEntity::getTaskStatus, 1, 2)
                .last("limit 1"));
        if (exist != null) return exist.getTaskNo();

        PayChannelCompensateEntity e = new PayChannelCompensateEntity();
        e.setTaskNo(buildTaskNo());
        e.setBizNo(req.getBizNo());
        e.setBizType(req.getBizType());
        e.setChannelCode(req.getChannelCode());
        e.setQueryCount(0);
        e.setMaxQuery(req.getMaxQuery() == null ? 10 : req.getMaxQuery());
        int delay = req.getDelaySeconds() == null ? 30 : req.getDelaySeconds();
        e.setNextExecuteTime(new Date(System.currentTimeMillis() + delay * 1000L));
        e.setLastResult("PENDING");
        e.setTaskStatus(1);
        e.setCreateTime(new Date());
        e.setUpdateTime(new Date());
        compensateMapper.insert(e);
        return e.getTaskNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeOne(Long id) {
        PayChannelCompensateEntity e = compensateMapper.selectById(id);
        if (e == null) throw PaymentException.bizError("补偿任务不存在");
        if (e.getTaskStatus() != null && e.getTaskStatus() >= 3) {
            log.info("[compensate/executeOne] skip finished task taskNo={}, status={}", e.getTaskNo(), e.getTaskStatus());
            return false;
        }

        // 锁定：置为执行中
        int locked = compensateMapper.update(null, new LambdaUpdateWrapper<PayChannelCompensateEntity>()
                .eq(PayChannelCompensateEntity::getId, id)
                .eq(PayChannelCompensateEntity::getTaskStatus, 1)
                .set(PayChannelCompensateEntity::getTaskStatus, 2)
                .set(PayChannelCompensateEntity::getUpdateTime, new Date()));
        if (locked == 0) return false;

        // 此处桩化为 PENDING（真实场景应调用渠道 query 接口）
        String queryResult = executeCompensation(e);
        int newCount = (e.getQueryCount() == null ? 0 : e.getQueryCount()) + 1;
        int maxQuery = e.getMaxQuery() == null ? 10 : e.getMaxQuery();
        int nextStatus;
        String finalStatus = null;
        String errorMsg = null;
        Date nextExecute = null;

        if ("SUCCESS".equals(queryResult) || "CLOSED".equals(queryResult)) {
            nextStatus = 3;
            finalStatus = queryResult;
        } else if ("FAILED".equals(queryResult)) {
            nextStatus = 4;
            finalStatus = queryResult;
            errorMsg = "渠道返回明确失败";
        } else if (newCount >= maxQuery) {
            nextStatus = 5;
            errorMsg = "达到最大查询次数 " + maxQuery + " 仍未有最终结果";
        } else {
            // 继续待执行
            nextStatus = 1;
            // 指数退避：30s, 60s, 120s, 240s, 480s ... 上限 3600s
            long delay = Math.min(3600L, 30L << Math.min(newCount - 1, 7));
            nextExecute = new Date(System.currentTimeMillis() + delay * 1000L);
        }

        LambdaUpdateWrapper<PayChannelCompensateEntity> uw = new LambdaUpdateWrapper<PayChannelCompensateEntity>()
                .eq(PayChannelCompensateEntity::getId, id)
                .set(PayChannelCompensateEntity::getQueryCount, newCount)
                .set(PayChannelCompensateEntity::getLastQueryTime, new Date())
                .set(PayChannelCompensateEntity::getLastResult, queryResult)
                .set(PayChannelCompensateEntity::getTaskStatus, nextStatus)
                .set(PayChannelCompensateEntity::getFinalStatus, finalStatus)
                .set(PayChannelCompensateEntity::getErrorMsg, errorMsg)
                .set(PayChannelCompensateEntity::getNextExecuteTime, nextExecute)
                .set(PayChannelCompensateEntity::getUpdateTime, new Date());
        compensateMapper.update(null, uw);
        log.info("[compensate/executeOne] taskNo={}, queryCount={}, newStatus={}", e.getTaskNo(), newCount, nextStatus);
        return true;
    }

    private String executeCompensation(PayChannelCompensateEntity task) {
        String bizNo = task.getBizNo();
        if (bizNo != null && bizNo.startsWith("PAY_NOTIFY:")) {
            String cashierNo = bizNo.substring("PAY_NOTIFY:".length());
            PayCashierOrderEntity order = cashierOrderMapper.selectByCashierNo(cashierNo);
            if (order == null) return "FAILED";
            if (CashierOrderStatusEnum.SUCCESS.getCode() != order.getStatus()) return "PENDING";
            return bizNotifyService.notifyPaySuccess(order) ? "SUCCESS" : "PENDING";
        }
        return "PENDING";
    }

    @Override
    public int executeDue(int limit) {
        int useLimit = Math.max(1, Math.min(limit, 200));
        List<PayChannelCompensateEntity> list = compensateMapper.selectList(new LambdaQueryWrapper<PayChannelCompensateEntity>()
                .eq(PayChannelCompensateEntity::getTaskStatus, 1)
                .le(PayChannelCompensateEntity::getNextExecuteTime, new Date())
                .orderByAsc(PayChannelCompensateEntity::getNextExecuteTime)
                .last("limit " + useLimit));
        int cnt = 0;
        for (PayChannelCompensateEntity t : list) {
            try {
                if (executeOne(t.getId())) cnt++;
            } catch (Exception ex) {
                log.error("[compensate/executeDue] taskNo={} error: {}", t.getTaskNo(), ex.getMessage(), ex);
            }
        }
        return cnt;
    }

    @Override
    public IPage<PayChannelCompensateEntity> page(CompensateListRequest req) {
        Page<PayChannelCompensateEntity> page = new Page<>(
                req.getPageNo() == null ? 1 : req.getPageNo(),
                req.getPageSize() == null ? 10 : req.getPageSize());
        LambdaQueryWrapper<PayChannelCompensateEntity> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getTaskNo()))      qw.eq(PayChannelCompensateEntity::getTaskNo, req.getTaskNo());
        if (StrUtil.isNotBlank(req.getBizNo()))       qw.eq(PayChannelCompensateEntity::getBizNo, req.getBizNo());
        if (StrUtil.isNotBlank(req.getChannelCode())) qw.eq(PayChannelCompensateEntity::getChannelCode, req.getChannelCode());
        if (req.getTaskStatus() != null)              qw.eq(PayChannelCompensateEntity::getTaskStatus, req.getTaskStatus());
        qw.orderByDesc(PayChannelCompensateEntity::getId);
        return compensateMapper.selectPage(page, qw);
    }

    @Override
    public PayChannelCompensateEntity detail(String taskNo) {
        return compensateMapper.selectOne(new LambdaQueryWrapper<PayChannelCompensateEntity>()
                .eq(PayChannelCompensateEntity::getTaskNo, taskNo));
    }

    @Override
    public boolean giveUp(String taskNo, String reason) {
        int rows = compensateMapper.update(null, new LambdaUpdateWrapper<PayChannelCompensateEntity>()
                .eq(PayChannelCompensateEntity::getTaskNo, taskNo)
                .lt(PayChannelCompensateEntity::getTaskStatus, 3)
                .set(PayChannelCompensateEntity::getTaskStatus, 5)
                .set(PayChannelCompensateEntity::getErrorMsg, StrUtil.blankToDefault(reason, "人工放弃"))
                .set(PayChannelCompensateEntity::getUpdateTime, new Date()));
        if (rows == 0) throw PaymentException.bizError("任务不存在或已结束");
        return true;
    }

    private String buildTaskNo() {
        return "CT" + System.currentTimeMillis() + IdUtil.fastSimpleUUID().substring(0, 6);
    }
}
