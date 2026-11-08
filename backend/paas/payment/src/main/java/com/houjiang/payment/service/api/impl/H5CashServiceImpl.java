package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.dto.h5cash.H5CashOrderRequest;
import com.houjiang.payment.dto.h5cash.H5CashOrderResponse;
import com.houjiang.payment.entity.H5CashOrderEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.mapper.H5CashOrderMapper;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.H5CashService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

/**
 * 通企付 H5 收银台服务实现
 * 状态机：0-生成 → 1-已下单 → 2-已支付/3-已退款/4-已关闭/5-异常
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class H5CashServiceImpl implements H5CashService {

    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    private final H5CashOrderMapper h5CashOrderMapper;

    /**
     * 27. H5 收银台下单
     * 步骤：参数校验 → 落库（state=0）→ 调用通企付 → 成功改 state=1+回填 H5Url+payOrderId / 失败改 state=5
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public H5CashOrderResponse getH5Url(H5CashOrderRequest request) {
        log.info("开始H5收银台下单, mchOrderNo:{}, amount:{}",
                request.getMchOrderNo(), request.getAmount());

        // 1. 参数校验
        validateH5CashOrder(request);
        // 2. 填充基础参数
        fillBaseRequest(request);

        // 3. 防重单：相同 mchNo+mchOrderNo 已存在 → 返回已有 H5Url（幂等）
        H5CashOrderEntity exist = h5CashOrderMapper.selectByMchOrderNo(request.getMchNo(), request.getMchOrderNo());
        if (exist != null) {
            if (StrUtil.isNotBlank(exist.getH5Url()) && Integer.valueOf(1).equals(exist.getState())) {
                log.info("订单已存在且已下单，直接返回已生成 H5Url, mchOrderNo:{}", request.getMchOrderNo());
                H5CashOrderResponse resp = new H5CashOrderResponse();
                resp.setCode("0");
                resp.setMsg("SUCCESS");
                resp.setH5Url(exist.getH5Url());
                resp.setReqSeqNo(exist.getReqSeqNo());
                return resp;
            }
            throw PaymentException.bizError("订单已存在且状态非已下单，禁止重复下单：" + request.getMchOrderNo());
        }

        // 4. 落库：state=0 生成
        H5CashOrderEntity entity = buildEntity(request);
        h5CashOrderMapper.insert(entity);
        log.debug("H5收银台订单已落库, id:{}, mchOrderNo:{}", entity.getId(), entity.getMchOrderNo());

        try {
            // 5. 生成签名
            signService.signRequest(request);

            // 6. 调用通企付 API
            String apiUrl = paymentConfig.getApiUrl() + "h5Cash/getH5Url";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("H5收银台下单请求: {}, 参数: {}", apiUrl, jsonRequest);

            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                    apiUrl,
                    jsonRequest,
                    paymentConfig.getConnectTimeout(),
                    paymentConfig.getReadTimeout()
            );

            // 7. 解析响应并验签
            H5CashOrderResponse response = JSONUtil.toBean(jsonResponse, H5CashOrderResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("H5收银台下单响应签名验证失败, response: {}", jsonResponse);
                h5CashOrderMapper.updateState(entity.getId(), 5, null, null, null);
                throw PaymentException.bizError("响应签名验证失败");
            }

            // 8. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("H5收银台下单失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                h5CashOrderMapper.updateState(entity.getId(), 5, null, null, null);
                throw PaymentException.bizError("H5收银台下单失败: " + response.getMsg());
            }

            // 9. 成功：流转 state=1 已下单 + 回填 H5Url
            h5CashOrderMapper.updateState(entity.getId(), 1, null, response.getH5Url(), null);
            log.info("H5收银台下单成功, mchOrderNo:{}, H5Url:{}", request.getMchOrderNo(), response.getH5Url());
            return response;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("H5收银台下单异常, mchOrderNo:{}", request.getMchOrderNo(), e);
            try {
                h5CashOrderMapper.updateState(entity.getId(), 5, null, null, null);
            } catch (Exception ignore) {
                log.warn("异常状态回写失败, id:{}", entity.getId());
            }
            throw PaymentException.systemError("H5收银台下单失败: " + e.getMessage());
        }
    }

    /**
     * 解析 jumpUrl 回跳参数
     * 通企付支付完成后会带 mchNo/mchOrderNo/payOrderId/amount/state/sign/signType/reqTime 跳转回前端
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public H5CashOrderEntity parseJumpResult(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw PaymentException.bizError("jumpUrl 参数为空");
        }
        String mchNo = params.get("mchNo");
        String mchOrderNo = params.get("mchOrderNo");
        String stateStr = params.get("state");
        if (StrUtil.isBlank(mchNo) || StrUtil.isBlank(mchOrderNo)) {
            throw PaymentException.bizError("mchNo / mchOrderNo 不能为空");
        }

        // 1. 验签（与通知签名复用同一逻辑）
        if (!signService.verifyNotify(params)) {
            log.error("jumpUrl 验签失败, params:{}", params);
            throw PaymentException.bizError("jumpUrl 签名校验失败");
        }

        // 2. 查本地订单
        H5CashOrderEntity entity = h5CashOrderMapper.selectByMchOrderNo(mchNo, mchOrderNo);
        if (entity == null) {
            log.error("jumpUrl 回跳但本地订单不存在, mchNo:{}, mchOrderNo:{}", mchNo, mchOrderNo);
            throw PaymentException.bizError("订单不存在: " + mchOrderNo);
        }

        // 3. state 流转：通企付 state=2 支付成功 / 4 已关闭 / 其它视为异常
        Integer remoteState;
        try {
            remoteState = StrUtil.isBlank(stateStr) ? 5 : Integer.parseInt(stateStr);
        } catch (NumberFormatException e) {
            remoteState = 5;
        }
        Date paySuccessTime = remoteState == 2 ? new Date() : null;
        String payOrderId = params.get("payOrderId");
        h5CashOrderMapper.updateState(entity.getId(), remoteState, payOrderId, null, paySuccessTime);

        // 4. 返回最新实体
        return h5CashOrderMapper.selectById(entity.getId());
    }

    /**
     * 商户订单号查询
     */
    @Override
    public H5CashOrderEntity queryByMchOrderNo(String mchNo, String mchOrderNo) {
        if (StrUtil.isBlank(mchOrderNo)) {
            throw PaymentException.bizError("mchOrderNo 不能为空");
        }
        if (StrUtil.isBlank(mchNo)) {
            mchNo = paymentConfig.getMerchantNo();
        }
        return h5CashOrderMapper.selectByMchOrderNo(mchNo, mchOrderNo);
    }

    /**
     * 根据请求构建落库实体（state=0 生成）
     */
    private H5CashOrderEntity buildEntity(H5CashOrderRequest request) {
        H5CashOrderEntity entity = new H5CashOrderEntity();
        entity.setMchNo(request.getMchNo());
        entity.setAppId(request.getAppId());
        entity.setMchOrderNo(request.getMchOrderNo());
        entity.setReqSeqNo(request.getReqSeqNo());
        entity.setAmount(Long.parseLong(request.getAmount()));
        entity.setCurrency("CNY");
        entity.setBody(request.getBody());
        entity.setGoodsInfo(request.getGoodsInfo());
        entity.setExtParam(request.getExtParam());
        entity.setNotifyUrl(request.getNotifyUrl());
        entity.setJumpUrl(request.getJumpUrl());
        entity.setFrontUrl(request.getFrontUrl());
        entity.setValidTime(request.getValidTime());
        entity.setExpiredTime(request.getExpiredTime());
        entity.setPayTypeInfo(request.getPayTypeInfo());
        entity.setDirectPayType(request.getDirectPayType());
        entity.setChannelExtra(request.getChannelExtra());
        entity.setState(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        return entity;
    }

    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(H5CashOrderRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());

        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }

        // 设置默认值
        if (request.getValidTime() == null) {
            // 默认120秒
            request.setValidTime(120);
        } else if (request.getValidTime() < 120) {
            log.warn("链接有效期不能小于120秒,已调整为120秒");
            request.setValidTime(120);
        // 72小时=259200秒
        } else if (request.getValidTime() > 259200) {
            log.warn("链接有效期不能大于72小时,已调整为72小时");
            request.setValidTime(259200);
        }

        if (request.getExpiredTime() == null) {
            // 默认1小时
            request.setExpiredTime(3600);
        }
    }

    /**
     * 验证H5收银台下单必填参数
     */
    private void validateH5CashOrder(H5CashOrderRequest request) {
        if (StrUtil.isBlank(request.getMchOrderNo())) {
            throw PaymentException.bizError("商户订单号不能为空");
        }
        if (StrUtil.isBlank(request.getAmount())) {
            throw PaymentException.bizError("金额不能为空");
        }
        if (StrUtil.isBlank(request.getBody())) {
            throw PaymentException.bizError("描述不能为空");
        }

        // 校验金额格式
        try {
            long amount = Long.parseLong(request.getAmount());
            if (amount <= 0) {
                throw PaymentException.bizError("金额必须大于0");
            }
        } catch (NumberFormatException e) {
            throw PaymentException.bizError("金额格式错误");
        }

        // 校验validTime范围
        if (request.getValidTime() != null) {
            if (request.getValidTime() < 120) {
                throw PaymentException.bizError("链接有效期不能小于120秒");
            }
            // 72小时=259200秒
            if (request.getValidTime() > 259200) {
                throw PaymentException.bizError("链接有效期不能大于72小时");
            }
        }

        // 校验goodsInfo格式(如果有)
        if (StrUtil.isNotBlank(request.getGoodsInfo())) {
            try {
                JSONUtil.parseArray(request.getGoodsInfo());
            } catch (Exception e) {
                throw PaymentException.bizError("商品信息格式错误,必须为JSONArray格式");
            }
        }

        // 校验channelExtra格式(如果有)
        if (StrUtil.isNotBlank(request.getChannelExtra())) {
            try {
                JSONUtil.parseObj(request.getChannelExtra());
            } catch (Exception e) {
                throw PaymentException.bizError("渠道参数格式错误,必须为JSON格式");
            }
        }

        // 校验directPayType(如果有)
        if (StrUtil.isNotBlank(request.getDirectPayType())) {
            if (!"WX_JSAPI".equals(request.getDirectPayType())
                    && !"ALI_QR".equals(request.getDirectPayType())) {
                throw PaymentException.bizError("不支持的直接支付方式: " + request.getDirectPayType()
                        + ",仅支持WX_JSAPI或ALI_QR");
            }
        }
    }
}
