package com.houjiang.payment.service.api.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.notify.PayNotifyDTO;
import com.houjiang.payment.dto.notify.RefundNotifyDTO;
import com.houjiang.payment.dto.trade.*;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.TradeService;
import com.houjiang.payment.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 交易服务实现类
 * 按照文档规范实现的支付交易服务
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;

    @Override
    public UnifiedOrderResponse unifiedOrder(UnifiedOrderRequest request) {
        log.info("开始处理统一下单, mchOrderNo:{}, wayCode:{}, amount:{}", 
                request.getMchOrderNo(), request.getWayCode(), request.getAmount());
        
        try {
            // 1. 参数校验
            validateUnifiedOrder(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
            
            // 3. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "pay/unifiedOrder";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("请求通企付API: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl, 
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 4. 解析响应并验签
            UnifiedOrderResponse response = JSONUtil.toBean(jsonResponse, UnifiedOrderResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("统一下单响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 5. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("统一下单失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("下单失败: " + response.getMsg());
            }
            
            // TODO: 6. 保存订单信息到数据库
            // orderService.saveOrder(request, response);
            
            log.info("统一下单成功, mchOrderNo:{}, payOrderId:{}", 
                    response.getMchOrderNo(), response.getPayOrderId());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("统一下单异常, mchOrderNo:" + request.getMchOrderNo(), e);
            throw PaymentException.systemError("下单失败: " + e.getMessage());
        }
    }

    @Override
    public QueryOrderResponse queryOrder(QueryOrderRequest request) {
        log.info("开始查询订单状态, mchOrderNo:{}, payOrderId:{}", 
                request.getMchOrderNo(), request.getPayOrderId());
        
        try {
            // 1. 填充基础参数
            fillBaseRequest(request);
            
            // 2. 生成签名
            signService.signRequest(request);
            
            // 3. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "pay/query";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("查询订单请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 4. 解析响应并验签
            QueryOrderResponse response = JSONUtil.toBean(jsonResponse, QueryOrderResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("查询订单响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 5. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("查询订单失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("查询订单失败: " + response.getMsg());
            }
            
            // TODO: 6. 同步订单状态到数据库
            // orderService.syncOrderStatus(response);
            
            log.info("查询订单成功, mchOrderNo:{}, state:{}", 
                    response.getMchOrderNo(), response.getState());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询订单异常, mchOrderNo:" + request.getMchOrderNo(), e);
            throw PaymentException.systemError("查询订单失败: " + e.getMessage());
        }
    }

    @Override
    public CloseOrderResponse closeOrder(CloseOrderRequest request) {
        log.info("开始关闭订单, mchOrderNo:{}, payOrderId:{}", 
                request.getMchOrderNo(), request.getPayOrderId());
        
        try {
            // 1. 填充基础参数
            fillBaseRequest(request);
            
            // 2. 生成签名
            signService.signRequest(request);
            
            // 3. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "pay/close";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("关闭订单请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 4. 解析响应并验签
            CloseOrderResponse response = JSONUtil.toBean(jsonResponse, CloseOrderResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("关闭订单响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 5. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("关闭订单失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("关闭订单失败: " + response.getMsg());
            }
            
            // TODO: 6. 更新数据库订单状态为已关闭
            // orderService.closeOrder(request.getMchOrderNo());
            
            log.info("关闭订单成功, mchOrderNo:{}", request.getMchOrderNo());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("关闭订单异常, mchOrderNo:" + request.getMchOrderNo(), e);
            throw PaymentException.systemError("关闭订单失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        log.info("开始处理退款, mchOrderNo:{}, mchRefundNo:{}, refundAmount:{}", 
                request.getMchOrderNo(), request.getMchRefundNo(), request.getRefundAmount());
        
        try {
            // 1. 填充基础参数
            fillBaseRequest(request);
            
            // 2. 生成签名
            signService.signRequest(request);
            
            // 3. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "refund/refundOrder";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("退款请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 4. 解析响应并验签
            RefundResponse response = JSONUtil.toBean(jsonResponse, RefundResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("退款响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 5. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("退款失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("退款失败: " + response.getMsg());
            }
            
            // TODO: 6. 保存退款记录到数据库
            // refundService.saveRefund(request, response);
            
            log.info("退款受理成功, mchRefundNo:{}, refundOrderId:{}", 
                    response.getMchRefundNo(), response.getRefundOrderId());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("退款异常, mchRefundNo:" + request.getMchRefundNo(), e);
            throw PaymentException.systemError("退款失败: " + e.getMessage());
        }
    }

    @Override
    public QueryRefundResponse queryRefund(QueryRefundRequest request) {
        log.info("开始查询退款状态, refundOrderId:{}, mchRefundNo:{}", 
                request.getRefundOrderId(), request.getMchRefundNo());
        
        try {
            // 1. 参数校验
            validateQueryRefund(request);
            
            // 2. 填充基础参数
            fillBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
                        
            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "refund/query";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("查询退款请求: {}, 参数: {}", apiUrl, jsonRequest);
                        
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
                        
            // 5. 解析响应并验签
            QueryRefundResponse response = JSONUtil.toBean(jsonResponse, QueryRefundResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("查询退款响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
                        
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("查询退款失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("查询退款失败: " + response.getMsg());
            }
                        
            // TODO: 7. 同步退款状态到数据库
            // refundService.syncRefundStatus(response);
            
            log.info("查询退款成功, refundOrderId:{}, state:{}", 
                    response.getRefundOrderId(), response.getState());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询退款异常, refundOrderId:" + request.getRefundOrderId(), e);
            throw PaymentException.systemError("查询退款失败: " + e.getMessage());
        }
    }

    @Override
    public String handlePayNotify(HttpServletRequest httpRequest) {
        log.info("接收到支付通知");
        
        try {
            // 1. 解析通知参数（获取全量参数）
            Map<String, String> params = parseNotifyParams(httpRequest);
            log.info("支付通知参数: {}", params);
            
            // 2. 转换为DTO对象
            PayNotifyDTO notifyDTO = mapToPayNotify(params);
            
            // 3. 基础参数校验
            validatePayNotify(notifyDTO);
            
            // 4. 验证签名
            if (!signService.verifyNotify(params)) {
                log.error("支付通知签名验证失败, mchOrderNo:{}", notifyDTO.getMchOrderNo());
                return PaymentConstants.NotifyResult.FAIL;
            }
            
            log.info("签名验证通过, mchOrderNo:{}, payOrderId:{}, state:{}", 
                    notifyDTO.getMchOrderNo(), notifyDTO.getPayOrderId(), notifyDTO.getState());
            
            // 5. 幂等性检查（防止重复通知）
            if (isDuplicateNotify(notifyDTO.getPayOrderId(), notifyDTO.getMchOrderNo())) {
                log.warn("重复的支付通知, payOrderId:{}, mchOrderNo:{}", 
                        notifyDTO.getPayOrderId(), notifyDTO.getMchOrderNo());
                // 重复通知仍然返回success，避免重试
                return PaymentConstants.NotifyResult.SUCCESS;
            }
            
            // 6. 查询订单信息
            // TODO: 从数据库查询订单记录
            // PayOrder order = orderService.getByMchOrderNo(notifyDTO.getMchOrderNo());
            // if (order == null) {
            //     log.error("订单不存在, mchOrderNo:{}", notifyDTO.getMchOrderNo());
            //     return PaymentConstants.NotifyResult.FAIL;
            // }
            
            // 7. 验证订单状态
            // if (order.getState() == PaymentConstants.OrderStatus.PAY_SUCCESS) {
            //     log.warn("订单已支付, mchOrderNo:{}", notifyDTO.getMchOrderNo());
            //     return PaymentConstants.NotifyResult.SUCCESS;
            // }
            
            // 8. 验证金额
            // if (!order.getAmount().equals(notifyDTO.getAmount())) {
            //     log.error("金额不匹配, orderAmount:{}, notifyAmount:{}", 
            //             order.getAmount(), notifyDTO.getAmount());
            //     return PaymentConstants.NotifyResult.FAIL;
            // }
            
            // 9. 更新订单状态
            // updateOrderStatus(notifyDTO);
            
            // 10. 执行业务回调（如发货、增加会员余额等）
            // executeBusinessCallback(notifyDTO);
            
            // 11. 记录通知日志
            // saveNotifyLog(notifyDTO);
            
            log.info("支付通知处理成功, mchOrderNo:{}, amount:{}, state:{}", 
                    notifyDTO.getMchOrderNo(), notifyDTO.getAmount(), notifyDTO.getState());
            
            // 必须返回小写"success"
            return PaymentConstants.NotifyResult.SUCCESS;
            
        } catch (PaymentException e) {
            log.error("支付通知处理失败: {}", e.getMessage(), e);
            return PaymentConstants.NotifyResult.FAIL;
        } catch (Exception e) {
            log.error("支付通知处理异常", e);
            return PaymentConstants.NotifyResult.FAIL;
        }
    }

    @Override
    public String handleRefundNotify(HttpServletRequest httpRequest) {
        log.info("接收到退款通知");
        
        try {
            // 1. 解析通知参数（获取全量参数）
            Map<String, String> params = parseNotifyParams(httpRequest);
            log.info("退款通知参数: {}", params);
            
            // 2. 转换为DTO对象
            RefundNotifyDTO notifyDTO = mapToRefundNotify(params);
            
            // 3. 基础参数校验
            validateRefundNotify(notifyDTO);
            
            // 4. 验证签名
            if (!signService.verifyNotify(params)) {
                log.error("退款通知签名验证失败, refundOrderId:{}", notifyDTO.getRefundOrderId());
                return PaymentConstants.NotifyResult.FAIL;
            }
            
            log.info("签名验证通过, refundOrderId:{}, payOrderId:{}, state:{}", 
                    notifyDTO.getRefundOrderId(), notifyDTO.getPayOrderId(), notifyDTO.getState());
            
            // 5. 幂等性检查（防止重复通知）
            if (isDuplicateRefundNotify(notifyDTO.getRefundOrderId(), notifyDTO.getMchRefundNo())) {
                log.warn("重复的退款通知, refundOrderId:{}, mchRefundNo:{}", 
                        notifyDTO.getRefundOrderId(), notifyDTO.getMchRefundNo());
                // 重复通知仍然返回success，避免重试
                return PaymentConstants.NotifyResult.SUCCESS;
            }
            
            // 6. 查询退款记录
            // TODO: 从数据库查询退款记录
            // RefundOrder refundOrder = refundService.getByRefundOrderId(notifyDTO.getRefundOrderId());
            // if (refundOrder == null) {
            //     log.error("退款记录不存在, refundOrderId:{}", notifyDTO.getRefundOrderId());
            //     return PaymentConstants.NotifyResult.FAIL;
            // }
            
            // 7. 验证退款状态
            // if (refundOrder.getState() == PaymentConstants.RefundStatus.SUCCESS) {
            //     log.warn("退款已成功, refundOrderId:{}", notifyDTO.getRefundOrderId());
            //     return PaymentConstants.NotifyResult.SUCCESS;
            // }
            
            // 8. 验证金额
            // if (!refundOrder.getRefundAmount().equals(notifyDTO.getRefundAmount())) {
            //     log.error("退款金额不匹配, orderAmount:{}, notifyAmount:{}", 
            //             refundOrder.getRefundAmount(), notifyDTO.getRefundAmount());
            //     return PaymentConstants.NotifyResult.FAIL;
            // }
            
            // 9. 更新退款状态
            // updateRefundStatus(notifyDTO);
            
            // 10. 执行业务回调（如退回会员余额、恢复库存等）
            // executeRefundCallback(notifyDTO);
            
            // 11. 记录通知日志
            // saveRefundNotifyLog(notifyDTO);
            
            log.info("退款通知处理成功, refundOrderId:{}, state:{}, refundAmount:{}", 
                    notifyDTO.getRefundOrderId(), notifyDTO.getState(), notifyDTO.getRefundAmount());
            
            // 必须返回小写"success"
            return PaymentConstants.NotifyResult.SUCCESS;
            
        } catch (PaymentException e) {
            log.error("退款通知处理失败: {}", e.getMessage(), e);
            return PaymentConstants.NotifyResult.FAIL;
        } catch (Exception e) {
            log.error("退款通知处理异常", e);
            return PaymentConstants.NotifyResult.FAIL;
        }
    }
    
    /**
     * 填充请求基础参数
     */
    private void fillBaseRequest(com.houjiang.payment.dto.base.PaymentBaseRequest request) {
        request.setMerchantNo(paymentConfig.getMerchantNo());
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        // 如果没有请求流水号，自动生成
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
    }
    
    /**
     * 解析HTTP请求参数
     */
    private Map<String, String> parseNotifyParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }
        
        return params;
    }
    
    /**
     * 将Map转换为PayNotifyDTO对象
     */
    private PayNotifyDTO mapToPayNotify(Map<String, String> params) {
        PayNotifyDTO notifyDTO = new PayNotifyDTO();
        
        // 使用Hutool的BeanUtil进行转换
        // 将String类型的参数转换为对应的类型
        notifyDTO.setOrgId(params.get("orgId"));
        notifyDTO.setMchNo(params.get("mchNo"));
        notifyDTO.setAppId(params.get("appId"));
        notifyDTO.setSign(params.get("sign"));
        notifyDTO.setSignType(params.get("signType"));
        notifyDTO.setPayOrderId(params.get("payOrderId"));
        notifyDTO.setMchOrderNo(params.get("mchOrderNo"));
        notifyDTO.setIfCode(params.get("ifCode"));
        notifyDTO.setWayCode(params.get("wayCode"));
        
        // 金额转换（分）
        if (StrUtil.isNotBlank(params.get("amount"))) {
            notifyDTO.setAmount(Integer.parseInt(params.get("amount")));
        }
        
        notifyDTO.setCurrency(params.get("currency"));
        
        // 状态转换
        if (StrUtil.isNotBlank(params.get("state"))) {
            notifyDTO.setState(Integer.parseInt(params.get("state")));
        }
        
        notifyDTO.setClientIp(params.get("clientIp"));
        notifyDTO.setBody(params.get("body"));
        notifyDTO.setMchName(params.get("mchName"));
        notifyDTO.setChannelUser(params.get("channelUser"));
        notifyDTO.setChannelUserName(params.get("channelUserName"));
        notifyDTO.setChannelAcctType(params.get("channelAcctType"));
        notifyDTO.setChannelOrderNo(params.get("channelOrderNo"));
        
        // 手续费转换
        if (StrUtil.isNotBlank(params.get("channelFeeAmount"))) {
            notifyDTO.setChannelFeeAmount(Long.parseLong(params.get("channelFeeAmount")));
        }
        
        notifyDTO.setChannelCusid(params.get("channelCusid"));
        notifyDTO.setErrCode(params.get("errCode"));
        notifyDTO.setErrMsg(params.get("errMsg"));
        notifyDTO.setExtParam(params.get("extParam"));
        
        // 时间戳转换
        if (StrUtil.isNotBlank(params.get("createdAt"))) {
            notifyDTO.setCreatedAt(Long.parseLong(params.get("createdAt")));
        }
        if (StrUtil.isNotBlank(params.get("successTime"))) {
            notifyDTO.setSuccessTime(Long.parseLong(params.get("successTime")));
        }
        if (StrUtil.isNotBlank(params.get("reqTime"))) {
            notifyDTO.setReqTime(Long.parseLong(params.get("reqTime")));
        }
        
        notifyDTO.setChannelBankCode(params.get("channelBankCode"));
        
        return notifyDTO;
    }
    
    /**
     * 验证支付通知必填参数
     */
    private void validatePayNotify(PayNotifyDTO notifyDTO) {
        if (StrUtil.isBlank(notifyDTO.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getPayOrderId())) {
            throw PaymentException.bizError("支付订单号不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getMchOrderNo())) {
            throw PaymentException.bizError("商户订单号不能为空");
        }
        if (notifyDTO.getAmount() == null || notifyDTO.getAmount() <= 0) {
            throw PaymentException.bizError("支付金额无效");
        }
        if (notifyDTO.getState() == null) {
            throw PaymentException.bizError("订单状态不能为空");
        }
    }
    
    /**
     * 检查是否为重复通知（幂等性检查）
     * TODO: 实际实现需要查询数据库或Redis缓存
     */
    private boolean isDuplicateNotify(String payOrderId, String mchOrderNo) {
        // TODO: 从数据库或Redis查询该订单是否已经处理过
        // 例如：
        // String cacheKey = "pay_notify:" + payOrderId;
        // Boolean exists = redisTemplate.hasKey(cacheKey);
        // if (exists) {
        //     return true;
        // }
        // // 设置缓存标记，防止重复处理（有效期1小时）
        // redisTemplate.opsForValue().set(cacheKey, "1", 1, TimeUnit.HOURS);
        
        return false;
    }
    
    /**
     * 验证退款查询必填参数
     */
    private void validateQueryRefund(QueryRefundRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        // refundOrderId和mchRefundNo至少传一个
        if (StrUtil.isBlank(request.getRefundOrderId()) && StrUtil.isBlank(request.getMchRefundNo())) {
            throw PaymentException.bizError("退款订单号和商户退款单号至少传一个");
        }
    }
    
    /**
     * 验证统一下单必填参数及不同支付方式的channelExtra约束
     */
    private void validateUnifiedOrder(UnifiedOrderRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getMchOrderNo())) {
            throw PaymentException.bizError("商户订单号不能为空");
        }
        if (StrUtil.isBlank(request.getWayCode())) {
            throw PaymentException.bizError("支付方式(wayCode)不能为空");
        }
        if (StrUtil.isBlank(request.getAmount())) {
            throw PaymentException.bizError("支付金额不能为空");
        }
        if (StrUtil.isBlank(request.getNotifyUrl())) {
            throw PaymentException.bizError("异步通知地址不能为空");
        }

        String wayCode = request.getWayCode();

        // JDBT_PAY、YT_PAY 必须提供客户端IP
        if (PaymentConstants.WayCode.JDBT_PAY.equals(wayCode)
                || PaymentConstants.WayCode.YT_PAY.equals(wayCode)) {
            if (StrUtil.isBlank(request.getClientIp())) {
                throw PaymentException.bizError("wayCode为JDBT_PAY或YT_PAY时clientIp不能为空");
            }
        }

        // 以下支付方式需要校验channelExtra
        if (PaymentConstants.WayCode.WX_TRANS.equals(wayCode)
                || PaymentConstants.WayCode.WX_B2B.equals(wayCode)
                || PaymentConstants.WayCode.WX_LITE.equals(wayCode)
                || PaymentConstants.WayCode.WX_JSAPI.equals(wayCode)
                || PaymentConstants.WayCode.ALI_JSAPI.equals(wayCode)
                || PaymentConstants.WayCode.YUNST2_WQF_ZZHK.equals(wayCode)
                || PaymentConstants.WayCode.AUTO_BAR.equals(wayCode)
                || PaymentConstants.WayCode.B2B_ORDER_PAY.equals(wayCode)
                || PaymentConstants.WayCode.YW_PAY.equals(wayCode)
                || PaymentConstants.WayCode.YT_PAY.equals(wayCode)
                || PaymentConstants.WayCode.JDBT_PAY.equals(wayCode)
                || PaymentConstants.WayCode.QUICK_PAY.equals(wayCode)) {

            if (StrUtil.isBlank(request.getChannelExtra())) {
                throw PaymentException.bizError("当前支付方式下channelExtra不能为空");
            }

            // 统一先校验为JSON
            if (!JSONUtil.isTypeJSON(request.getChannelExtra())) {
                throw PaymentException.bizError("channelExtra必须为JSON格式字符串");
            }

            cn.hutool.json.JSONObject extra = JSONUtil.parseObj(request.getChannelExtra());

            // 微信预消费/微信B2B门店助手/微信小程序/微信公众号支付
            if (PaymentConstants.WayCode.WX_TRANS.equals(wayCode)
                    || PaymentConstants.WayCode.WX_B2B.equals(wayCode)
                    || PaymentConstants.WayCode.WX_LITE.equals(wayCode)
                    || PaymentConstants.WayCode.WX_JSAPI.equals(wayCode)) {
                String openid = extra.getStr("openid");
                String subAppid = extra.getStr("subAppid");
                if (StrUtil.isBlank(openid)) {
                    throw PaymentException.bizError("channelExtra中openid不能为空");
                }
                if (StrUtil.isBlank(subAppid)) {
                    throw PaymentException.bizError("channelExtra中subAppid不能为空");
                }
            }

            // 支付宝公众号支付
            if (PaymentConstants.WayCode.ALI_JSAPI.equals(wayCode)) {
                String buyerUserId = extra.getStr("buyerUserId");
                if (StrUtil.isBlank(buyerUserId)) {
                    throw PaymentException.bizError("channelExtra中buyerUserId不能为空");
                }
                // fqnum 为非必填,此处不强校验取值
            }

            // 微企付：goodsinfo + frontCallbackUrl
            if (PaymentConstants.WayCode.YUNST2_WQF_ZZHK.equals(wayCode)) {
                if (!extra.containsKey("goodsinfo")) {
                    throw PaymentException.bizError("微企付支付时channelExtra.goodsinfo不能为空");
                }
                if (!extra.containsKey("frontCallbackUrl")) {
                    throw PaymentException.bizError("微企付支付时channelExtra.frontCallbackUrl不能为空");
                }
            }

            // 付款码支付：authcode + terminfo
            if (PaymentConstants.WayCode.AUTO_BAR.equals(wayCode)) {
                String authcode = extra.getStr("authcode");
                String terminfo = extra.getStr("terminfo");
                if (StrUtil.isBlank(authcode)) {
                    throw PaymentException.bizError("付款码支付时channelExtra.authcode不能为空");
                }
                if (StrUtil.isBlank(terminfo)) {
                    throw PaymentException.bizError("付款码支付时channelExtra.terminfo不能为空");
                }
            }

            // B2B订单付款：goodsinfo + acctno + acctname + accttype + bankcode
            if (PaymentConstants.WayCode.B2B_ORDER_PAY.equals(wayCode)) {
                if (!extra.containsKey("goodsinfo")) {
                    throw PaymentException.bizError("B2B订单付款时channelExtra.goodsinfo不能为空");
                }
                if (StrUtil.isBlank(extra.getStr("acctno"))) {
                    throw PaymentException.bizError("B2B订单付款时channelExtra.acctno不能为空");
                }
                if (StrUtil.isBlank(extra.getStr("acctname"))) {
                    throw PaymentException.bizError("B2B订单付款时channelExtra.acctname不能为空");
                }
                if (StrUtil.isBlank(extra.getStr("accttype"))) {
                    throw PaymentException.bizError("B2B订单付款时channelExtra.accttype不能为空");
                }
                if (StrUtil.isBlank(extra.getStr("bankcode"))) {
                    throw PaymentException.bizError("B2B订单付款时channelExtra.bankcode不能为空");
                }
            }

            // 云微支付：apptype + appname
            if (PaymentConstants.WayCode.YW_PAY.equals(wayCode)) {
                if (StrUtil.isBlank(extra.getStr("apptype"))) {
                    throw PaymentException.bizError("云微支付时channelExtra.apptype不能为空");
                }
                if (StrUtil.isBlank(extra.getStr("appname"))) {
                    throw PaymentException.bizError("云微支付时channelExtra.appname不能为空");
                }
            }

            // 云梯计划：extendparams中封装ytPlanInfo,这里只校验extendparams存在
            if (PaymentConstants.WayCode.YT_PAY.equals(wayCode)) {
                if (!extra.containsKey("extendparams")) {
                    throw PaymentException.bizError("云梯计划支付时channelExtra.extendparams不能为空");
                }
            }

            // 白条分期：需要传入分期期数fqnum
            if (PaymentConstants.WayCode.JDBT_PAY.equals(wayCode)) {
                if (StrUtil.isBlank(extra.getStr("fqnum"))) {
                    throw PaymentException.bizError("白条分期支付时channelExtra.fqnum不能为空");
                }
            }

            // 快捷支付：协议编号agreeid必填
            if (PaymentConstants.WayCode.QUICK_PAY.equals(wayCode)) {
                if (StrUtil.isBlank(extra.getStr("agreeid"))) {
                    throw PaymentException.bizError("快捷支付时channelExtra.agreeid不能为空");
                }
            }
        }
    }
    
    /**
     * 将Map转换为RefundNotifyDTO对象
     */    private RefundNotifyDTO mapToRefundNotify(Map<String, String> params) {
        RefundNotifyDTO notifyDTO = new RefundNotifyDTO();
        
        // 基础信息
        notifyDTO.setOrgId(params.get("orgId"));
        notifyDTO.setMchNo(params.get("mchNo"));
        notifyDTO.setAppId(params.get("appId"));
        notifyDTO.setSign(params.get("sign"));
        notifyDTO.setSignType(params.get("signType"));
        notifyDTO.setRefundOrderId(params.get("refundOrderId"));
        notifyDTO.setPayOrderId(params.get("payOrderId"));
        notifyDTO.setMchRefundNo(params.get("mchRefundNo"));
        
        // 金额转换（分）
        if (StrUtil.isNotBlank(params.get("payAmount"))) {
            notifyDTO.setPayAmount(Integer.parseInt(params.get("payAmount")));
        }
        if (StrUtil.isNotBlank(params.get("refundAmount"))) {
            notifyDTO.setRefundAmount(Integer.parseInt(params.get("refundAmount")));
        }
        
        notifyDTO.setCurrency(params.get("currency"));
        
        // 状态转换
        if (StrUtil.isNotBlank(params.get("state"))) {
            notifyDTO.setState(Integer.parseInt(params.get("state")));
        }
        
        notifyDTO.setChannelOrderNo(params.get("channelOrderNo"));
        
        // 手续费转换
        if (StrUtil.isNotBlank(params.get("channelFeeAmount"))) {
            notifyDTO.setChannelFeeAmount(Long.parseLong(params.get("channelFeeAmount")));
        }
        
        notifyDTO.setErrCode(params.get("errCode"));
        notifyDTO.setErrMsg(params.get("errMsg"));
        notifyDTO.setExtParam(params.get("extParam"));
        
        // 时间戳转换
        if (StrUtil.isNotBlank(params.get("createdAt"))) {
            notifyDTO.setCreatedAt(Long.parseLong(params.get("createdAt")));
        }
        if (StrUtil.isNotBlank(params.get("successTime"))) {
            notifyDTO.setSuccessTime(Long.parseLong(params.get("successTime")));
        }
        if (StrUtil.isNotBlank(params.get("reqTime"))) {
            notifyDTO.setReqTime(Long.parseLong(params.get("reqTime")));
        }
        
        return notifyDTO;
    }
    
    /**
     * 验证退款通知必填参数
     */
    private void validateRefundNotify(RefundNotifyDTO notifyDTO) {
        if (StrUtil.isBlank(notifyDTO.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getRefundOrderId())) {
            throw PaymentException.bizError("退款订单号不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getPayOrderId())) {
            throw PaymentException.bizError("支付订单号不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getMchRefundNo())) {
            throw PaymentException.bizError("商户退款单号不能为空");
        }
        if (notifyDTO.getRefundAmount() == null || notifyDTO.getRefundAmount() <= 0) {
            throw PaymentException.bizError("退款金额无效");
        }
        if (notifyDTO.getState() == null) {
            throw PaymentException.bizError("退款状态不能为空");
        }
    }
    
    /**
     * 检查是否为重复退款通知（幂等性检查）
     * TODO: 实际实现需要查询数据库或Redis缓存
     */
    private boolean isDuplicateRefundNotify(String refundOrderId, String mchRefundNo) {
        // TODO: 从数据库或Redis查询该退款订单是否已经处理过
        // 例如：
        // String cacheKey = "refund_notify:" + refundOrderId;
        // Boolean exists = redisTemplate.hasKey(cacheKey);
        // if (exists) {
        //     return true;
        // }
        // // 设置缓存标记，防止重复处理（有效期1小时）
        // redisTemplate.opsForValue().set(cacheKey, "1", 1, TimeUnit.HOURS);
        
        return false;
    }
}
