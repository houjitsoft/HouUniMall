package com.houjiang.payment.service.api.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.dto.mch.MchRegRequest;
import com.houjiang.payment.dto.mch.MchRegResponse;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.MchService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 商户服务实现类
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MchServiceImpl implements MchService {

    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;

    @Override
    public MchRegResponse mchReg(MchRegRequest request) {
        log.info("开始商户进件, companyName:{}, cusid:{}, loginUsername:{}",
                request.getCompanyName(), request.getCusid(), request.getLoginUsername());

        try {
            // 1. 参数校验
            validateMchReg(request);

            // 2. 填充基础参数
            fillBaseRequest(request);

            // 3. 生成签名
            signService.signRequest(request);

            // 4. 调用通企付商户进件API
            String apiUrl = paymentConfig.getApiUrl() + "mch/reg";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("商户进件请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            MchRegResponse response = JSONUtil.toBean(jsonResponse, MchRegResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("商户进件响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 5. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("商户进件失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("商户进件失败: " + response.getMsg());
            }

            // TODO: 6. 落库记录商户信息
            // merchantService.saveMerchant(request, response);

            log.info("商户进件处理完成, companyName:{}, mchNo:{}, appId:{}, code:{}, msg:{}",
                    request.getCompanyName(), response.getMchNo(), response.getAppId(),
                    response.getCode(), response.getMsg());
            return response;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("商户进件异常", e);
            throw PaymentException.systemError("商户进件失败: " + e.getMessage());
        }
    }

    /**
     * 商户进件参数校验
     */
    private void validateMchReg(MchRegRequest request) {
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("服务商appId不能为空");
        }
        if (StrUtil.isBlank(request.getCompanyName())) {
            throw PaymentException.bizError("企业名称不能为空");
        }
        if (StrUtil.isBlank(request.getMchShortName())) {
            throw PaymentException.bizError("企业简称不能为空");
        }
        if (StrUtil.isBlank(request.getUniCredit())) {
            throw PaymentException.bizError("统一社会信用代码不能为空");
        }
        if (StrUtil.isBlank(request.getPhone())) {
            throw PaymentException.bizError("法人/负责人手机号不能为空");
        }
        if (StrUtil.isBlank(request.getLoginUsername())) {
            throw PaymentException.bizError("登录账号不能为空");
        }
        if (StrUtil.isBlank(request.getCusid())) {
            throw PaymentException.bizError("收银宝商户号不能为空");
        }

        // 手机号长度校验
        if (StrUtil.isNotBlank(request.getPhone()) && request.getPhone().length() != 11) {
            throw PaymentException.bizError("手机号长度必须为11位");
        }

        // 登录账号格式：字母开头6-18位
        if (StrUtil.isNotBlank(request.getLoginUsername())) {
            String loginUsername = request.getLoginUsername();
            if (loginUsername.length() < 6 || loginUsername.length() > 18
                    || !Character.isLetter(loginUsername.charAt(0))) {
                throw PaymentException.bizError("登录账号格式不正确,需字母开头6-18位");
            }
        }

        // 收银宝商户号长度校验：15位
        if (StrUtil.isNotBlank(request.getCusid()) && request.getCusid().length() != 15) {
            throw PaymentException.bizError("收银宝商户号长度必须为15位");
        }
    }

    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(MchRegRequest request) {
        // appId 允许外部显式传入,未传时使用配置默认值
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        // 请求时间
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());

        // 如果没有请求流水号,自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
    }
}
