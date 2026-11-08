package com.houjiang.payment.service.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.houjiang.payment.entity.PayMemberProductEntity;
import com.houjiang.payment.entity.PayMerchantEntity;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayMemberProductMapper;
import com.houjiang.payment.mapper.PayMerchantMapper;
import com.houjiang.payment.mapper.PayUserResourceMapper;
import com.houjiang.payment.service.api.MemberBizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自研会员业务服务实现（W4 骨架）。
 *
 * 设计原则：复用既有 hou_pay_user_resource / hou_pay_merchant，
 * 新增字段聚合在 hou_pay_member_product 表（W4 patches 引入）。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberBizServiceImpl implements MemberBizService {

    private final PayMemberProductMapper memberProductMapper;
    private final PayMerchantMapper merchantMapper;
    private final PayUserResourceMapper userResourceMapper;

    /**
     * 会员信息聚合查询：返回 memberId、余额、产品列表。
     */
    @Override
    public Map<String, Object> infoQuery(Map<String, Object> req) {
        String memberId = strVal(req, "memberId");
        if (!StringUtils.hasText(memberId)) {
            throw PaymentException.bizError("memberId 必填");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("memberId", memberId);

        // 余额（来自 hou_pay_user_resource，userId=memberId）
        try {
            PayUserResourceEntity res = userResourceMapper.selectById(memberId);
            result.put("userMoney", res == null ? 0 : res.getUserMoney());
            result.put("userMoneyFrozen", res == null ? 0 : res.getUserMoneyFrozen());
        } catch (Exception e) {
            log.warn("[member/biz/info] 查询余额失败 memberId={}", memberId, e);
            result.put("userMoney", 0);
        }

        // 已开通产品
        Long mid;
        try {
            mid = Long.parseLong(memberId);
        } catch (NumberFormatException e) {
            mid = null;
        }
        List<PayMemberProductEntity> products = mid == null
                ? Collections.emptyList()
                : memberProductMapper.selectList(new LambdaQueryWrapper<PayMemberProductEntity>()
                .eq(PayMemberProductEntity::getMemberId, mid)
                .orderByDesc(PayMemberProductEntity::getCreateTime));
        result.put("productCount", products.size());
        result.put("products", products);
        return result;
    }

    /**
     * 企业会员信息修改：仅修改商户表非核心字段。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enterpriseModify(Map<String, Object> req, String operatorId) {
        String mchNo = strVal(req, "mchNo");
        if (!StringUtils.hasText(mchNo)) {
            throw PaymentException.bizError("mchNo 必填");
        }
        LambdaUpdateWrapper<PayMerchantEntity> u = new LambdaUpdateWrapper<>();
        u.eq(PayMerchantEntity::getMchNo, mchNo);
        boolean any = false;
        String contactName = strVal(req, "contactName");
        if (StringUtils.hasText(contactName)) {
            u.set(PayMerchantEntity::getContactName, contactName);
            any = true;
        }
        String contactPhone = strVal(req, "contactPhone");
        if (StringUtils.hasText(contactPhone)) {
            u.set(PayMerchantEntity::getContactPhone, contactPhone);
            any = true;
        }
        String address = strVal(req, "address");
        if (StringUtils.hasText(address)) {
            u.set(PayMerchantEntity::getAddress, address);
            any = true;
        }
        if (!any) {
            throw PaymentException.bizError("至少提供一个可修改字段（contactName/contactPhone/address）");
        }
        u.set(PayMerchantEntity::getUpdateTime, new Date());
        int n = merchantMapper.update(null, u);
        log.info("[member/biz/enterpriseModify] mchNo={} rows={} operator={}", mchNo, n, operatorId);
        return n > 0;
    }

    /**
     * 会员注册状态查询：依据商户表 status 推断。
     */
    @Override
    public Integer regStatus(Map<String, Object> req) {
        String mchNo = strVal(req, "mchNo");
        if (!StringUtils.hasText(mchNo)) {
            return 0;
        }
        PayMerchantEntity m = merchantMapper.selectOne(new LambdaQueryWrapper<PayMerchantEntity>()
                .eq(PayMerchantEntity::getMchNo, mchNo));
        if (m == null) return 0;
        // 0未注册 1已注册 2待审核 3已注销，根据 status 映射
        Integer s = m.getStatus();
        if (s == null) return 2;
        // 正常
        if (s == 1) return 1;
        // 注销
        if (s == 2) return 3;
        // 其余视为待审核
        return 2;
    }

    /**
     * 会员产品管理：open/close/list 三种 action。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PayMemberProductEntity> productManage(Map<String, Object> req, String operatorId) {
        String action = strVal(req, "action");
        if (!StringUtils.hasText(action)) {
            throw PaymentException.bizError("action 必填（open/close/list）");
        }
        String memberId = strVal(req, "memberId");
        if (!StringUtils.hasText(memberId)) {
            throw PaymentException.bizError("memberId 必填");
        }
        Long mid = Long.parseLong(memberId);
        List<PayMemberProductEntity> ret = new ArrayList<>();
        switch (action) {
            case "list":
                ret.addAll(memberProductMapper.selectList(new LambdaQueryWrapper<PayMemberProductEntity>()
                        .eq(PayMemberProductEntity::getMemberId, mid)
                        .orderByDesc(PayMemberProductEntity::getCreateTime)));
                break;
            case "open": {
                String productCode = strVal(req, "productCode");
                if (!StringUtils.hasText(productCode)) {
                    throw PaymentException.bizError("productCode 必填");
                }
                PayMemberProductEntity p = memberProductMapper.selectOne(new LambdaQueryWrapper<PayMemberProductEntity>()
                        .eq(PayMemberProductEntity::getMemberId, mid)
                        .eq(PayMemberProductEntity::getProductCode, productCode));
                if (p == null) {
                    p = new PayMemberProductEntity();
                    p.setMemberId(mid);
                    p.setMchNo(strVal(req, "mchNo"));
                    p.setProductCode(productCode);
                    p.setProductName(strVal(req, "productName"));
                    p.setStatus(1);
                    p.setOpenTime(new Date());
                    p.setCreateTime(new Date());
                    p.setUpdateTime(new Date());
                    p.setRemark("operator=" + operatorId);
                    memberProductMapper.insert(p);
                } else {
                    p.setStatus(1);
                    p.setOpenTime(new Date());
                    p.setUpdateTime(new Date());
                    memberProductMapper.updateById(p);
                }
                ret.add(p);
                break;
            }
            case "close": {
                String productCode = strVal(req, "productCode");
                if (!StringUtils.hasText(productCode)) {
                    throw PaymentException.bizError("productCode 必填");
                }
                LambdaUpdateWrapper<PayMemberProductEntity> u = new LambdaUpdateWrapper<>();
                u.eq(PayMemberProductEntity::getMemberId, mid)
                 .eq(PayMemberProductEntity::getProductCode, productCode)
                 .set(PayMemberProductEntity::getStatus, 0)
                 .set(PayMemberProductEntity::getCloseTime, new Date())
                 .set(PayMemberProductEntity::getUpdateTime, new Date());
                memberProductMapper.update(null, u);
                break;
            }
            default:
                throw PaymentException.bizError("不支持的 action: " + action);
        }
        log.info("[member/biz/productManage] action={} memberId={} operator={}", action, memberId, operatorId);
        return ret;
    }

    private String strVal(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v == null ? null : v.toString();
    }
}
