package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.payment.dto.risk.RiskLogAuditRequest;
import com.houjiang.payment.dto.risk.RiskLogListRequest;
import com.houjiang.payment.entity.PayRiskLogEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayRiskLogMapper;
import com.houjiang.payment.service.api.RiskLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * 风控日志服务实现
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskLogServiceImpl implements RiskLogService {

    private final PayRiskLogMapper logMapper;

    @Override
    public IPage<PayRiskLogEntity> list(RiskLogListRequest req) {
        Page<PayRiskLogEntity> page = new Page<>(
                req.getPageNum() == null ? 1 : req.getPageNum(),
                req.getPageSize() == null ? 20 : req.getPageSize());
        LambdaQueryWrapper<PayRiskLogEntity> qw = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getLogNo()))  qw.eq(PayRiskLogEntity::getLogNo, req.getLogNo());
        if (StrUtil.isNotBlank(req.getBizNo()))  qw.eq(PayRiskLogEntity::getBizNo, req.getBizNo());
        if (req.getBizType() != null)            qw.eq(PayRiskLogEntity::getBizType, req.getBizType());
        if (StrUtil.isNotBlank(req.getMchNo()))  qw.eq(PayRiskLogEntity::getMchNo, req.getMchNo());
        if (StrUtil.isNotBlank(req.getUserId())) qw.eq(PayRiskLogEntity::getUserId, req.getUserId());
        if (req.getAction() != null)             qw.eq(PayRiskLogEntity::getAction, req.getAction());
        if (req.getRiskLevel() != null)          qw.eq(PayRiskLogEntity::getRiskLevel, req.getRiskLevel());
        if (req.getAuditStatus() != null)        qw.eq(PayRiskLogEntity::getAuditStatus, req.getAuditStatus());
        if (req.getStartTime() != null)          qw.ge(PayRiskLogEntity::getCreateTime, req.getStartTime());
        if (req.getEndTime() != null)            qw.le(PayRiskLogEntity::getCreateTime, req.getEndTime());
        qw.orderByDesc(PayRiskLogEntity::getId);
        return logMapper.selectPage(page, qw);
    }

    @Override
    public PayRiskLogEntity detail(String logNo) {
        return logMapper.selectOne(new LambdaQueryWrapper<PayRiskLogEntity>()
                .eq(PayRiskLogEntity::getLogNo, logNo));
    }

    @Override
    public boolean audit(RiskLogAuditRequest req, String operatorId) {
        if (req.getAuditStatus() == null || (req.getAuditStatus() != 1 && req.getAuditStatus() != 2)) {
            throw PaymentException.bizError("审核状态非法：1-通过 2-驳回");
        }
        PayRiskLogEntity exist = logMapper.selectOne(new LambdaQueryWrapper<PayRiskLogEntity>()
                .eq(PayRiskLogEntity::getLogNo, req.getLogNo()));
        if (exist == null) throw PaymentException.bizError("风控日志不存在");
        if (exist.getAction() == null || exist.getAction() != 4) {
            throw PaymentException.bizError("仅待审核(action=4)的日志可审核");
        }
        int rows = logMapper.update(null, new LambdaUpdateWrapper<PayRiskLogEntity>()
                .eq(PayRiskLogEntity::getLogNo, req.getLogNo())
                .set(PayRiskLogEntity::getAuditStatus, req.getAuditStatus())
                .set(PayRiskLogEntity::getAuditResult, req.getAuditResult())
                .set(PayRiskLogEntity::getAuditOperator, operatorId)
                .set(PayRiskLogEntity::getAuditTime, new Date()));
        return rows > 0;
    }

    @Override
    public boolean submitReview(Map<String, Object> req, String operatorId) {
        String logNo = (String) req.get("logNo");
        if (StrUtil.isBlank(logNo)) throw PaymentException.bizError("logNo 不能为空");
        PayRiskLogEntity exist = logMapper.selectOne(new LambdaQueryWrapper<PayRiskLogEntity>()
                .eq(PayRiskLogEntity::getLogNo, logNo));
        if (exist == null) throw PaymentException.bizError("风控日志不存在");
        if (exist.getReviewStatus() != null && exist.getReviewStatus() != 0) {
            throw PaymentException.bizError("当前记录已提交人工审核，请勿重复提交");
        }
        log.info("[risk/log/submitReview] logNo={} operator={}", logNo, operatorId);
        int rows = logMapper.update(null, new LambdaUpdateWrapper<PayRiskLogEntity>()
                .eq(PayRiskLogEntity::getLogNo, logNo)
                .set(PayRiskLogEntity::getReviewStatus, 0)
                .set(PayRiskLogEntity::getReviewRemark, req.get("remark")));
        return rows > 0;
    }

    @Override
    public boolean approve(Map<String, Object> req, String operatorId) {
        String logNo = (String) req.get("logNo");
        if (StrUtil.isBlank(logNo)) throw PaymentException.bizError("logNo 不能为空");
        Object rsObj = req.get("reviewStatus");
        if (rsObj == null) throw PaymentException.bizError("reviewStatus 不能为空");
        Integer reviewStatus = Integer.valueOf(rsObj.toString());
        if (reviewStatus != 1 && reviewStatus != 2) {
            throw PaymentException.bizError("reviewStatus 非法：1-通过 2-驳回");
        }
        PayRiskLogEntity exist = logMapper.selectOne(new LambdaQueryWrapper<PayRiskLogEntity>()
                .eq(PayRiskLogEntity::getLogNo, logNo));
        if (exist == null) throw PaymentException.bizError("风控日志不存在");
        if (exist.getReviewStatus() == null || exist.getReviewStatus() != 0) {
            throw PaymentException.bizError("仅待审(reviewStatus=0)的记录可审核");
        }
        Long reviewUserId = null;
        try { reviewUserId = Long.valueOf(operatorId); } catch (Exception ignored) {}
        log.info("[risk/log/approve] logNo={} reviewStatus={} operator={}", logNo, reviewStatus, operatorId);
        int rows = logMapper.update(null, new LambdaUpdateWrapper<PayRiskLogEntity>()
                .eq(PayRiskLogEntity::getLogNo, logNo)
                .set(PayRiskLogEntity::getReviewStatus, reviewStatus)
                .set(PayRiskLogEntity::getReviewUserId, reviewUserId)
                .set(PayRiskLogEntity::getReviewTime, new Date())
                .set(PayRiskLogEntity::getReviewRemark, req.get("remark")));
        return rows > 0;
    }
}
