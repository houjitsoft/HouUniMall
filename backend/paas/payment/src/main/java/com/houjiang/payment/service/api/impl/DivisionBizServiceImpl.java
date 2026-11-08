package com.houjiang.payment.service.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.houjiang.payment.entity.PayDivisionReceiverEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayDivisionReceiverMapper;
import com.houjiang.payment.service.api.DivisionBizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 自研分账业务服务实现（W4 骨架）。
 *
 * 设计：
 *  - 接收方表 hou_pay_division_receiver 预配置（W4 patches 引入）
 *  - 规则引擎骨架按 shareRatio 万分比线性拆分；真实规则后续接入 Drools/独立规则模块
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DivisionBizServiceImpl implements DivisionBizService {

    private final PayDivisionReceiverMapper receiverMapper;

    /** 接收方业务编号前缀 */
    private static final String RECEIVER_NO_PREFIX = "DR";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayDivisionReceiverEntity receiverBind(Map<String, Object> req, String operatorId) {
        String mchNo = strVal(req, "mchNo");
        String receiverType = strVal(req, "receiverType");
        String receiverName = strVal(req, "receiverName");
        String accountNo = strVal(req, "accountNo");
        if (!StringUtils.hasText(mchNo) || !StringUtils.hasText(receiverType)
                || !StringUtils.hasText(receiverName) || !StringUtils.hasText(accountNo)) {
            throw PaymentException.bizError("mchNo/receiverType/receiverName/accountNo 必填");
        }
        if (!receiverType.matches("PERSON|MERCHANT|PLATFORM")) {
            throw PaymentException.bizError("receiverType 仅支持 PERSON/MERCHANT/PLATFORM");
        }

        PayDivisionReceiverEntity r = new PayDivisionReceiverEntity();
        r.setMchNo(mchNo);
        r.setReceiverNo(RECEIVER_NO_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase());
        r.setReceiverType(receiverType);
        r.setReceiverName(receiverName);
        r.setAccountType(strVal(req, "accountType"));
        r.setAccountNo(accountNo);
        r.setAccountBank(strVal(req, "accountBank"));
        r.setIdCard(strVal(req, "idCard"));
        r.setPhone(strVal(req, "phone"));
        Object ratio = req.get("shareRatio");
        r.setShareRatio(ratio == null ? 0 : Integer.parseInt(ratio.toString()));
        Object pri = req.get("priority");
        r.setPriority(pri == null ? 100 : Integer.parseInt(pri.toString()));
        Object minShare = req.get("minShareAmount");
        if (minShare != null && StringUtils.hasText(minShare.toString())) {
            r.setMinShareAmount(Long.parseLong(minShare.toString()));
        }
        Object maxShare = req.get("maxShareAmount");
        if (maxShare != null && StringUtils.hasText(maxShare.toString())) {
            r.setMaxShareAmount(Long.parseLong(maxShare.toString()));
        }
        Object fallback = req.get("isFallback");
        if (fallback != null && StringUtils.hasText(fallback.toString())) {
            r.setIsFallback(Integer.parseInt(fallback.toString()));
        } else {
            r.setIsFallback(0);
        }
        r.setStatus(1);
        r.setRemark(strVal(req, "remark"));
        try {
            r.setCreateUserId(operatorId == null ? null : Long.parseLong(operatorId));
        } catch (NumberFormatException e) {
            r.setCreateUserId(null);
        }
        r.setCreateTime(new Date());
        r.setUpdateTime(new Date());
        receiverMapper.insert(r);
        log.info("[division/biz/receiverBind] mch={} receiverNo={} type={} operator={}",
                mchNo, r.getReceiverNo(), receiverType, operatorId);
        return r;
    }

    @Override
    public List<PayDivisionReceiverEntity> receiverList(Map<String, Object> req) {
        String mchNo = strVal(req, "mchNo");
        if (!StringUtils.hasText(mchNo)) {
            throw PaymentException.bizError("mchNo 必填");
        }
        LambdaQueryWrapper<PayDivisionReceiverEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(PayDivisionReceiverEntity::getMchNo, mchNo);
        Object statusObj = req.get("status");
        if (statusObj != null) {
            qw.eq(PayDivisionReceiverEntity::getStatus, Integer.parseInt(statusObj.toString()));
        }
        qw.orderByAsc(PayDivisionReceiverEntity::getPriority).orderByDesc(PayDivisionReceiverEntity::getCreateTime);
        return receiverMapper.selectList(qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean receiverUnbind(Map<String, Object> req, String operatorId) {
        String receiverNo = strVal(req, "receiverNo");
        if (!StringUtils.hasText(receiverNo)) {
            throw PaymentException.bizError("receiverNo 必填");
        }
        LambdaUpdateWrapper<PayDivisionReceiverEntity> u = new LambdaUpdateWrapper<>();
        u.eq(PayDivisionReceiverEntity::getReceiverNo, receiverNo)
         .set(PayDivisionReceiverEntity::getStatus, 0)
         .set(PayDivisionReceiverEntity::getUpdateTime, new Date());
        int n = receiverMapper.update(null, u);
        log.info("[division/biz/receiverUnbind] receiverNo={} rows={} operator={}", receiverNo, n, operatorId);
        return n > 0;
    }

    /**
     * 规则引擎计算（增强版）：
     * 1. 按优先级排序，先处理普通接收方，最后处理兆底接收方
     * 2. 支持 minShareAmount（最低保障）、maxShareAmount（封顶限制）
     * 3. isFallback=1 的接收方获取剩余全部金额
     *
     * @param req 包含 mchNo, totalAmount(分)
     * @return 分账明细列表
     */
    @Override
    public List<Map<String, Object>> ruleEngineCalc(Map<String, Object> req) {
        String mchNo = strVal(req, "mchNo");
        Object totalAmountObj = req.get("totalAmount");
        if (!StringUtils.hasText(mchNo) || totalAmountObj == null) {
            throw PaymentException.bizError("mchNo/totalAmount 必填");
        }
        long totalAmount = Long.parseLong(totalAmountObj.toString());
    
        List<PayDivisionReceiverEntity> receivers = receiverMapper.selectList(
                new LambdaQueryWrapper<PayDivisionReceiverEntity>()
                        .eq(PayDivisionReceiverEntity::getMchNo, mchNo)
                        .eq(PayDivisionReceiverEntity::getStatus, 1)
                        .orderByAsc(PayDivisionReceiverEntity::getPriority));
    
        if (receivers.isEmpty()) {
            throw PaymentException.bizError("商户 " + mchNo + " 无可用分账接收方");
        }
    
        // 分离普通接收方与兆底接收方
        List<PayDivisionReceiverEntity> normalReceivers = new ArrayList<>();
        PayDivisionReceiverEntity fallbackReceiver = null;
        for (PayDivisionReceiverEntity r : receivers) {
            if (r.getIsFallback() != null && r.getIsFallback() == 1) {
                fallbackReceiver = r;
            } else {
                normalReceivers.add(r);
            }
        }
    
        List<Map<String, Object>> result = new ArrayList<>();
        long allocated = 0L;
    
        // 第一轮：处理普通接收方（按优先级顺序）
        for (PayDivisionReceiverEntity r : normalReceivers) {
            int ratio = r.getShareRatio() == null ? 0 : r.getShareRatio();
            long share = totalAmount * ratio / 10000L;
    
            // 应用最低保障
            if (r.getMinShareAmount() != null && r.getMinShareAmount() > 0 && share < r.getMinShareAmount()) {
                share = r.getMinShareAmount();
            }
            // 应用封顶限制
            if (r.getMaxShareAmount() != null && r.getMaxShareAmount() > 0 && share > r.getMaxShareAmount()) {
                share = r.getMaxShareAmount();
            }
            // 不能超过剩余可分配金额
            if (allocated + share > totalAmount) {
                share = totalAmount - allocated;
            }
    
            allocated += share;
            Map<String, Object> item = new HashMap<>();
            item.put("receiverNo", r.getReceiverNo());
            item.put("receiverName", r.getReceiverName());
            item.put("receiverType", r.getReceiverType());
            item.put("shareRatio", ratio);
            item.put("shareAmount", share);
            item.put("priority", r.getPriority());
            item.put("isFallback", 0);
            result.add(item);
        }
    
        // 第二轮：兆底接收方获取全部余额
        if (fallbackReceiver != null) {
            long remainder = totalAmount - allocated;
            Map<String, Object> item = new HashMap<>();
            item.put("receiverNo", fallbackReceiver.getReceiverNo());
            item.put("receiverName", fallbackReceiver.getReceiverName());
            item.put("receiverType", fallbackReceiver.getReceiverType());
            item.put("shareRatio", fallbackReceiver.getShareRatio());
            item.put("shareAmount", remainder);
            item.put("priority", fallbackReceiver.getPriority());
            item.put("isFallback", 1);
            result.add(item);
        } else if (allocated < totalAmount && !normalReceivers.isEmpty()) {
            // 无兆底方时，余额归最后一个普通接收方
            Map<String, Object> last = result.get(result.size() - 1);
            long lastShare = (Long) last.get("shareAmount");
            last.put("shareAmount", lastShare + (totalAmount - allocated));
        }
    
        log.info("[division/biz/ruleEngineCalc] mch={} total={} receivers={} allocated={}",
                mchNo, totalAmount, result.size(), allocated);
        return result;
    }

    private String strVal(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v == null ? null : v.toString();
    }
}
