package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.payment.dto.mch.MerchantAccountUpdateRequest;
import com.houjiang.payment.dto.mch.MerchantDetailVO;
import com.houjiang.payment.dto.mch.MerchantFeeConfigRequest;
import com.houjiang.payment.dto.mch.MerchantListRequest;
import com.houjiang.payment.dto.mch.MerchantProductOpenRequest;
import com.houjiang.payment.dto.mch.MerchantRegRequest;
import com.houjiang.payment.dto.mch.MerchantStatusChangeRequest;
import com.houjiang.payment.dto.mch.MerchantUpdateRequest;
import com.houjiang.payment.entity.PayMerchantEntity;
import com.houjiang.payment.entity.PayMerchantFeeEntity;
import com.houjiang.payment.entity.PayMerchantProductEntity;
import com.houjiang.payment.entity.PayMerchantQualCertEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.PayMerchantFeeMapper;
import com.houjiang.payment.mapper.PayMerchantMapper;
import com.houjiang.payment.mapper.PayMerchantProductMapper;
import com.houjiang.payment.mapper.PayMerchantQualCertMapper;
import com.houjiang.payment.service.api.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 平台商户管理服务实现
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final PayMerchantMapper merchantMapper;
    private final PayMerchantProductMapper productMapper;
    private final PayMerchantFeeMapper feeMapper;
    private final PayMerchantQualCertMapper qualCertMapper;

    /** 商户号前缀（对标通企付 E + 12位数字） */
    private static final String MCH_NO_PREFIX = "E";

    /** ================== 进件 ================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayMerchantEntity register(MerchantRegRequest request, String operatorId) {
        log.info("[merchant/reg] companyName={}, legalPhone={}, mchType={}",
                request.getCompanyName(), request.getLegalPhone(), request.getMchType());

        // 1. 基本校验
        if (request.getMchType() == null) {
            throw PaymentException.bizError("商户类型不能为空");
        }
        if (StrUtil.isBlank(request.getCompanyName())) {
            throw PaymentException.bizError("企业/商户名称不能为空");
        }
        if (StrUtil.isBlank(request.getLegalName()) || StrUtil.isBlank(request.getLegalIdNo())
                || StrUtil.isBlank(request.getLegalPhone())) {
            throw PaymentException.bizError("法人姓名/证件号/手机号不能为空");
        }
        // 企业必须有统一社会信用代码
        if (request.getMchType() != null && request.getMchType() == 1
                && StrUtil.isBlank(request.getUniCredit())) {
            throw PaymentException.bizError("企业商户必须填写统一社会信用代码");
        }

        // 2. 重复性校验：同一证件号 + 手机号 + 名称
        Long exist = merchantMapper.selectCount(new LambdaQueryWrapper<PayMerchantEntity>()
                .eq(PayMerchantEntity::getLegalIdNo, request.getLegalIdNo())
                .eq(PayMerchantEntity::getCompanyName, request.getCompanyName())
                .ne(PayMerchantEntity::getStatus, 4));
        if (exist != null && exist > 0) {
            throw PaymentException.bizError("该商户已进件，请勿重复提交");
        }

        // 3. 生成商户号
        String mchNo = generateMchNo();

        // 4. 落库
        PayMerchantEntity entity = new PayMerchantEntity();
        entity.setMchNo(mchNo);
        entity.setAppId(request.getOrgId() != null ? request.getOrgId() + "_" + mchNo : mchNo);
        entity.setMchType(request.getMchType());
        entity.setCompanyName(request.getCompanyName());
        entity.setMchShortName(request.getMchShortName());
        entity.setUniCredit(request.getUniCredit());
        entity.setLicenseImg(request.getLicenseImg());
        entity.setLegalName(request.getLegalName());
        entity.setLegalIdType(request.getLegalIdType() == null ? 1 : request.getLegalIdType());
        entity.setLegalIdNo(request.getLegalIdNo());
        entity.setLegalPhone(request.getLegalPhone());
        entity.setLegalEmail(request.getLegalEmail());
        entity.setLegalIdFront(request.getLegalIdFront());
        entity.setLegalIdBack(request.getLegalIdBack());
        entity.setContactName(request.getContactName());
        entity.setContactPhone(request.getContactPhone());
        entity.setContactEmail(request.getContactEmail());
        entity.setProvinceCode(request.getProvinceCode());
        entity.setCityCode(request.getCityCode());
        entity.setDistrictCode(request.getDistrictCode());
        entity.setAddress(request.getAddress());
        entity.setBusinessScope(request.getBusinessScope());
        entity.setIndustryCode(request.getIndustryCode());
        entity.setLoginUsername(request.getLoginUsername());
        entity.setCusid(request.getCusid());
        entity.setAgencyNo(request.getAgencyNo());
        entity.setOrgId(request.getOrgId());
        // 待审核
        entity.setStatus(0);
        entity.setSettleType(request.getSettleType());
        entity.setSettleBankCode(request.getSettleBankCode());
        entity.setSettleBankName(request.getSettleBankName());
        entity.setSettleBranchName(request.getSettleBranchName());
        entity.setSettleAccountName(request.getSettleAccountName());
        entity.setSettleAccountNo(request.getSettleAccountNo());
        entity.setSettleReservedMobile(request.getSettleReservedMobile());
        entity.setSettleCycle(request.getSettleCycle() == null ? 1 : request.getSettleCycle());
        entity.setCreatorId(operatorId);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setDeleted(0);
        merchantMapper.insert(entity);
        log.info("[merchant/reg] success, mchNo={}, id={}", mchNo, entity.getId());
        return entity;
    }

    /** ================== 列表 ================== */

    @Override
    public IPage<PayMerchantEntity> list(MerchantListRequest request) {
        Page<PayMerchantEntity> page = new Page<>(
                request.getPageNo() == null || request.getPageNo() < 1 ? 1 : request.getPageNo(),
                request.getPageSize() == null || request.getPageSize() < 1 ? 20 : request.getPageSize()
        );
        LambdaQueryWrapper<PayMerchantEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(request.getMchNo()), PayMerchantEntity::getMchNo, request.getMchNo());
        wrapper.eq(request.getMchType() != null, PayMerchantEntity::getMchType, request.getMchType());
        wrapper.like(StrUtil.isNotBlank(request.getCompanyName()), PayMerchantEntity::getCompanyName, request.getCompanyName());
        wrapper.eq(request.getStatus() != null, PayMerchantEntity::getStatus, request.getStatus());
        wrapper.eq(StrUtil.isNotBlank(request.getLegalPhone()), PayMerchantEntity::getLegalPhone, request.getLegalPhone());
        wrapper.ge(StrUtil.isNotBlank(request.getStartDate()), PayMerchantEntity::getCreateTime, request.getStartDate());
        wrapper.le(StrUtil.isNotBlank(request.getEndDate()), PayMerchantEntity::getCreateTime, request.getEndDate() + " 23:59:59");
        wrapper.orderByDesc(PayMerchantEntity::getCreateTime);
        return merchantMapper.selectPage(page, wrapper);
    }

    /** ================== 详情 ================== */

    @Override
    public MerchantDetailVO detail(String mchNo) {
        if (StrUtil.isBlank(mchNo)) {
            throw PaymentException.bizError("商户号不能为空");
        }
        PayMerchantEntity merchant = merchantMapper.selectOne(new LambdaQueryWrapper<PayMerchantEntity>()
                .eq(PayMerchantEntity::getMchNo, mchNo));
        if (merchant == null) {
            throw PaymentException.bizError("商户不存在: " + mchNo);
        }
        List<PayMerchantProductEntity> products = productMapper.selectList(new LambdaQueryWrapper<PayMerchantProductEntity>()
                .eq(PayMerchantProductEntity::getMchNo, mchNo)
                .orderByDesc(PayMerchantProductEntity::getCreateTime));
        List<PayMerchantFeeEntity> fees = feeMapper.selectList(new LambdaQueryWrapper<PayMerchantFeeEntity>()
                .eq(PayMerchantFeeEntity::getMchNo, mchNo)
                .orderByDesc(PayMerchantFeeEntity::getEffectiveTime));

        MerchantDetailVO vo = new MerchantDetailVO();
        vo.setMerchant(merchant);
        vo.setProducts(products);
        vo.setFees(fees);
        return vo;
    }

    /** ================== 信息更新 ================== */

    @Override
    public boolean update(MerchantUpdateRequest request, String operatorId) {
        PayMerchantEntity merchant = merchantMapper.selectOne(new LambdaQueryWrapper<PayMerchantEntity>()
                .eq(PayMerchantEntity::getMchNo, request.getMchNo()));
        if (merchant == null) {
            throw PaymentException.bizError("商户不存在: " + request.getMchNo());
        }
        LambdaUpdateWrapper<PayMerchantEntity> u = new LambdaUpdateWrapper<>();
        u.eq(PayMerchantEntity::getMchNo, request.getMchNo());
        if (StrUtil.isNotBlank(request.getMchShortName())) u.set(PayMerchantEntity::getMchShortName, request.getMchShortName());
        if (StrUtil.isNotBlank(request.getLicenseImg())) u.set(PayMerchantEntity::getLicenseImg, request.getLicenseImg());
        if (StrUtil.isNotBlank(request.getLegalEmail())) u.set(PayMerchantEntity::getLegalEmail, request.getLegalEmail());
        if (StrUtil.isNotBlank(request.getLegalIdFront())) u.set(PayMerchantEntity::getLegalIdFront, request.getLegalIdFront());
        if (StrUtil.isNotBlank(request.getLegalIdBack())) u.set(PayMerchantEntity::getLegalIdBack, request.getLegalIdBack());
        if (StrUtil.isNotBlank(request.getContactName())) u.set(PayMerchantEntity::getContactName, request.getContactName());
        if (StrUtil.isNotBlank(request.getContactPhone())) u.set(PayMerchantEntity::getContactPhone, request.getContactPhone());
        if (StrUtil.isNotBlank(request.getContactEmail())) u.set(PayMerchantEntity::getContactEmail, request.getContactEmail());
        if (StrUtil.isNotBlank(request.getProvinceCode())) u.set(PayMerchantEntity::getProvinceCode, request.getProvinceCode());
        if (StrUtil.isNotBlank(request.getCityCode())) u.set(PayMerchantEntity::getCityCode, request.getCityCode());
        if (StrUtil.isNotBlank(request.getDistrictCode())) u.set(PayMerchantEntity::getDistrictCode, request.getDistrictCode());
        if (StrUtil.isNotBlank(request.getAddress())) u.set(PayMerchantEntity::getAddress, request.getAddress());
        if (StrUtil.isNotBlank(request.getBusinessScope())) u.set(PayMerchantEntity::getBusinessScope, request.getBusinessScope());
        if (StrUtil.isNotBlank(request.getIndustryCode())) u.set(PayMerchantEntity::getIndustryCode, request.getIndustryCode());
        u.set(PayMerchantEntity::getUpdateTime, new Date());
        int n = merchantMapper.update(null, u);
        log.info("[merchant/update] mchNo={}, rows={}, operator={}", request.getMchNo(), n, operatorId);
        return n > 0;
    }

    /** ================== 状态变更 ================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(MerchantStatusChangeRequest request, String operatorId) {
        PayMerchantEntity merchant = merchantMapper.selectOne(new LambdaQueryWrapper<PayMerchantEntity>()
                .eq(PayMerchantEntity::getMchNo, request.getMchNo()));
        if (merchant == null) {
            throw PaymentException.bizError("商户不存在: " + request.getMchNo());
        }
        Integer target = request.getStatus();
        if (target == null || target < 0 || target > 5) {
            throw PaymentException.bizError("非法状态");
        }
        // 简单状态机校验
        Integer from = merchant.getStatus();
        // 已注销不允许再变更
        if (from != null && from == 4) {
            throw PaymentException.bizError("商户已注销，不能再变更状态");
        }

        LambdaUpdateWrapper<PayMerchantEntity> u = new LambdaUpdateWrapper<>();
        u.eq(PayMerchantEntity::getMchNo, request.getMchNo());
        u.set(PayMerchantEntity::getStatus, target);
        u.set(PayMerchantEntity::getStatusRemark, request.getRemark());
        u.set(PayMerchantEntity::getUpdateTime, new Date());
        if (target == 2) {
            u.set(PayMerchantEntity::getApprovedTime, new Date());
            u.set(PayMerchantEntity::getApprovedBy, operatorId);
        } else if (target == 3) {
            u.set(PayMerchantEntity::getFrozenTime, new Date());
            u.set(PayMerchantEntity::getFrozenReason, request.getRemark());
        }
        int n = merchantMapper.update(null, u);
        log.info("[merchant/changeStatus] mchNo={}, from={} -> to={}, rows={}", request.getMchNo(), from, target, n);
        return n > 0;
    }

    /** ================== 费率配置 ================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayMerchantFeeEntity configFee(MerchantFeeConfigRequest request, String operatorId) {
        // 同一 (mchNo, productCode, payMethod) 视为同一条费率：先失效旧记录，再插入新记录
        LambdaQueryWrapper<PayMerchantFeeEntity> q = new LambdaQueryWrapper<PayMerchantFeeEntity>()
                .eq(PayMerchantFeeEntity::getMchNo, request.getMchNo())
                .eq(PayMerchantFeeEntity::getProductCode, request.getProductCode())
                .eq(PayMerchantFeeEntity::getStatus, 1);
        if (StrUtil.isNotBlank(request.getPayMethod())) {
            q.eq(PayMerchantFeeEntity::getPayMethod, request.getPayMethod());
        } else {
            q.isNull(PayMerchantFeeEntity::getPayMethod);
        }
        List<PayMerchantFeeEntity> olds = feeMapper.selectList(q);
        for (PayMerchantFeeEntity old : olds) {
            old.setStatus(0);
            old.setExpireTime(new Date());
            old.setUpdateTime(new Date());
            feeMapper.updateById(old);
        }

        PayMerchantFeeEntity fee = new PayMerchantFeeEntity();
        fee.setMchNo(request.getMchNo());
        fee.setProductCode(request.getProductCode());
        fee.setPayMethod(request.getPayMethod());
        fee.setFeeType(request.getFeeType());
        fee.setFeeRate(request.getFeeRate());
        fee.setFeeFixed(request.getFeeFixed());
        fee.setFeeMin(request.getFeeMin());
        fee.setFeeMax(request.getFeeMax());
        fee.setEffectiveTime(request.getEffectiveTime() == null ? new Date() : request.getEffectiveTime());
        fee.setExpireTime(request.getExpireTime());
        fee.setStatus(1);
        fee.setCreateTime(new Date());
        fee.setUpdateTime(new Date());
        fee.setDeleted(0);
        feeMapper.insert(fee);
        log.info("[merchant/configFee] mchNo={}, product={}, method={}, feeId={}",
                request.getMchNo(), request.getProductCode(), request.getPayMethod(), fee.getId());
        return fee;
    }

    /** ================== 结算账户更新 ================== */

    @Override
    public boolean updateSettleAccount(MerchantAccountUpdateRequest request, String operatorId) {
        PayMerchantEntity merchant = merchantMapper.selectOne(new LambdaQueryWrapper<PayMerchantEntity>()
                .eq(PayMerchantEntity::getMchNo, request.getMchNo()));
        if (merchant == null) {
            throw PaymentException.bizError("商户不存在: " + request.getMchNo());
        }
        LambdaUpdateWrapper<PayMerchantEntity> u = new LambdaUpdateWrapper<>();
        u.eq(PayMerchantEntity::getMchNo, request.getMchNo());
        u.set(PayMerchantEntity::getSettleType, request.getSettleType());
        u.set(PayMerchantEntity::getSettleBankCode, request.getSettleBankCode());
        u.set(PayMerchantEntity::getSettleBankName, request.getSettleBankName());
        u.set(PayMerchantEntity::getSettleBranchName, request.getSettleBranchName());
        u.set(PayMerchantEntity::getSettleAccountName, request.getSettleAccountName());
        u.set(PayMerchantEntity::getSettleAccountNo, request.getSettleAccountNo());
        u.set(PayMerchantEntity::getSettleReservedMobile, request.getSettleReservedMobile());
        if (request.getSettleCycle() != null) {
            u.set(PayMerchantEntity::getSettleCycle, request.getSettleCycle());
        }
        u.set(PayMerchantEntity::getUpdateTime, new Date());
        int n = merchantMapper.update(null, u);
        log.info("[merchant/updateAccount] mchNo={}, rows={}, operator={}", request.getMchNo(), n, operatorId);
        return n > 0;
    }

    /** ================== 产品开通 ================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayMerchantProductEntity openProduct(MerchantProductOpenRequest request, String operatorId) {
        PayMerchantEntity merchant = merchantMapper.selectOne(new LambdaQueryWrapper<PayMerchantEntity>()
                .eq(PayMerchantEntity::getMchNo, request.getMchNo()));
        if (merchant == null) {
            throw PaymentException.bizError("商户不存在: " + request.getMchNo());
        }
        // 同一商户 + 产品 仅保留一条有效记录，采用 upsert
        PayMerchantProductEntity exist = productMapper.selectOne(new LambdaQueryWrapper<PayMerchantProductEntity>()
                .eq(PayMerchantProductEntity::getMchNo, request.getMchNo())
                .eq(PayMerchantProductEntity::getProductCode, request.getProductCode()));
        Integer targetStatus = request.getStatus() == null ? 1 : request.getStatus();

        if (exist != null) {
            exist.setProductName(request.getProductName());
            exist.setStatus(targetStatus);
            exist.setSingleLimit(request.getSingleLimit());
            exist.setDailyLimit(request.getDailyLimit());
            exist.setMonthlyLimit(request.getMonthlyLimit());
            exist.setRemark(request.getRemark());
            if (targetStatus == 1) {
                exist.setOpenTime(new Date());
                exist.setCloseTime(null);
            } else if (targetStatus == 2) {
                exist.setCloseTime(new Date());
            }
            exist.setUpdateTime(new Date());
            productMapper.updateById(exist);
            return exist;
        }

        PayMerchantProductEntity p = new PayMerchantProductEntity();
        p.setMchNo(request.getMchNo());
        p.setProductCode(request.getProductCode());
        p.setProductName(request.getProductName());
        p.setStatus(targetStatus);
        p.setSingleLimit(request.getSingleLimit());
        p.setDailyLimit(request.getDailyLimit());
        p.setMonthlyLimit(request.getMonthlyLimit());
        p.setRemark(request.getRemark());
        if (targetStatus == 1) {
            p.setOpenTime(new Date());
        }
        p.setCreateTime(new Date());
        p.setUpdateTime(new Date());
        p.setDeleted(0);
        productMapper.insert(p);
        log.info("[merchant/openProduct] mchNo={}, product={}, status={}, id={}",
                request.getMchNo(), request.getProductCode(), targetStatus, p.getId());
        return p;
    }

    // ================== 工具方法 ==================

    /**
     * 生成商户号：E + yyyyMMdd + 4位随机数（兼 13 位），对标通企付 E249974037860 格式
     */
    private String generateMchNo() {
        for (int i = 0; i < 5; i++) {
            // 取8位
            long ts = System.currentTimeMillis() % 100000000L;
            // 4位
            int rand = new Random().nextInt(9000) + 1000;
            String no = MCH_NO_PREFIX + ts + rand;
            Long cnt = merchantMapper.selectCount(new LambdaQueryWrapper<PayMerchantEntity>()
                    .eq(PayMerchantEntity::getMchNo, no));
            if (cnt == null || cnt == 0) {
                return no;
            }
        }
        throw PaymentException.systemError("商户号生成失败，请重试");
    }

    // ================== W4 自研补全 3 接口 ==================

    /**
     * 个人商户进件（骨架）。
     * 区别于 register（企业），本接口 mchType=3 个人/2 个体工商户。
     * TODO: 后续接实名核验+银卡四要素。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayMerchantEntity regPerson(Map<String, Object> req, String operatorId) {
        if (req == null) throw PaymentException.bizError("参数不能为空");
        String mchName = strVal(req, "mchName");
        String personIdNo = strVal(req, "personIdNo");
        String personName = strVal(req, "personName");
        String contactPhone = strVal(req, "contactPhone");
        if (StrUtil.isBlank(mchName) || StrUtil.isBlank(personIdNo) || StrUtil.isBlank(personName)) {
            throw PaymentException.bizError("mchName/personIdNo/personName 必填");
        }
        Object mchTypeObj = req.get("mchType");
        Integer mchType = mchTypeObj == null ? 3 : Integer.parseInt(mchTypeObj.toString());
        if (mchType != 2 && mchType != 3) {
            throw PaymentException.bizError("本接口仅受理 mchType=2(个体工商户) / 3(个人)");
        }

        PayMerchantEntity m = new PayMerchantEntity();
        m.setMchNo(generateMchNo());
        m.setMchType(mchType);
        m.setCompanyName(mchName);
        m.setMchShortName(mchName);
        m.setLegalName(personName);
        m.setLegalIdType(1);
        m.setLegalIdNo(personIdNo);
        m.setLegalPhone(contactPhone);
        m.setContactName(personName);
        m.setContactPhone(contactPhone);
        // 0=待审核
        m.setStatus(0);
        m.setCreateTime(new Date());
        m.setUpdateTime(new Date());
        merchantMapper.insert(m);
        log.info("[merchant/regPerson] mchNo={} mchType={} operator={}", m.getMchNo(), mchType, operatorId);
        return m;
    }

    /**
     * 资质补件：向 hou_pay_merchant_qual_cert 补登证书。
     * 需提供 qualId（hou_pay_merchant_qual.id）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean qualSupplement(Map<String, Object> req, String operatorId) {
        if (req == null) throw PaymentException.bizError("参数不能为空");
        Object qualIdObj = req.get("qualId");
        String certType = strVal(req, "certType");
        String certPath = strVal(req, "certPath");
        if (qualIdObj == null || StrUtil.isBlank(certType) || StrUtil.isBlank(certPath)) {
            throw PaymentException.bizError("qualId/certType/certPath 必填");
        }
        PayMerchantQualCertEntity cert = new PayMerchantQualCertEntity();
        cert.setQualId(Long.parseLong(qualIdObj.toString()));
        cert.setCertType(certType);
        cert.setCertPath(certPath);
        cert.setCertSerialNo(strVal(req, "certSerialNo"));
        Object expireObj = req.get("expireTime");
        if (expireObj != null && StrUtil.isNotBlank(expireObj.toString())) {
            try {
                cert.setExpireTime(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(expireObj.toString()));
            } catch (Exception ignore) {
                // 格式不合仅警告，不阻断补件
                log.warn("[merchant/qualSupplement] expireTime 解析失败 raw={}", expireObj);
            }
        }
        cert.setCreateTime(new Date());
        qualCertMapper.insert(cert);
        log.info("[merchant/qualSupplement] qualId={} certType={} operator={}", cert.getQualId(), certType, operatorId);
        return Boolean.TRUE;
    }

    /**
     * 子商户/子账户列表（骨架）。
     * TODO: 待 sub_account 表设计完成后接入。
     */
    @Override
    public List<Map<String, Object>> subAccountList(Map<String, Object> req) {
        log.info("[merchant/subAccountList] req={}", req);
        return java.util.Collections.emptyList();
    }

    private String strVal(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v == null ? null : v.toString();
    }
}
