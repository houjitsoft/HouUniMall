package com.houjiang.payment.service.api.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.StrUtil;
import com.houjiang.payment.config.TqfPaymentConfig;
import com.houjiang.payment.constant.PaymentConstants;
import com.houjiang.payment.dto.division.*;
import com.houjiang.payment.dto.notify.DivisionNotifyDTO;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.SignService;
import com.houjiang.payment.service.api.DivisionService;
import com.houjiang.payment.util.TimeUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 分账服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DivisionServiceImpl implements DivisionService {
    
    private final SignService signService;
    private final TqfPaymentConfig paymentConfig;
    
    @Override
    public BatchDivisionResponse batchDivision(BatchDivisionRequest request) {
        log.info("开始批量分账, chanNo:{}, batchDivisionInfos:{}", 
                request.getChanNo(), request.getBatchDivisionInfos());
        
        try {
            // 1. 参数校验
            validateBatchDivision(request);
            
            // 2. 解析分账列表
            List<BatchDivisionInfoDTO> divisionList = parseBatchDivisionInfos(request.getBatchDivisionInfos());
            
            // 3. 根据渠道校验分账列表
            validateDivisionList(request.getChanNo(), divisionList);
            
            // 4. 填充基础参数
            fillBaseRequest(request);
            
            // 5. 生成签名
            signService.signRequest(request);
            
            // 6. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "assureDivision/batchDivision";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("批量分账请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 7. 解析响应并验签
            BatchDivisionResponse response = JSONUtil.toBean(jsonResponse, BatchDivisionResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("批量分账响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 8. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("批量分账失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("批量分账失败: " + response.getMsg());
            }
            
            // TODO: 9. 保存分账记录到数据库
            
            log.info("批量分账成功, batchNo:{}, state:{}", response.getBatchNo(), response.getState());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量分账异常", e);
            throw PaymentException.systemError("批量分账失败: " + e.getMessage());
        }
    }
    
    @Override
    public String handleDivisionNotify(HttpServletRequest httpRequest) {
        log.info("收到分账结果通知");
        
        try {
            // 1-5. 解析、校验、验签、幂等性检查
            Map<String, String> params = parseNotifyParams(httpRequest);
            DivisionNotifyDTO notifyDTO = mapToDivisionNotify(params);
            validateDivisionNotify(notifyDTO);
            
            if (!signService.verifyNotify(params)) {
                log.error("分账通知签名验证失败");
                return PaymentConstants.NotifyResult.FAIL;
            }
            
            if (isDuplicateDivisionNotify(notifyDTO.getAssureOrderId(), notifyDTO.getMchAssureOrderNo())) {
                return PaymentConstants.NotifyResult.SUCCESS;
            }
            
            // TODO: 6-11. 更新订单状态、执行回调等
            
            return PaymentConstants.NotifyResult.SUCCESS;
            
        } catch (Exception e) {
            log.error("分账通知处理异常", e);
            return PaymentConstants.NotifyResult.FAIL;
        }
    }
    
    @Override
    public QueryDivisionResponse queryDivision(QueryDivisionRequest request) {
        log.info("开始查询分账订单, mchAssureOrderNo:{}, assureOrderId:{}", 
                request.getMchAssureOrderNo(), request.getAssureOrderId());
        
        try {
            // 1. 参数校验
            validateQueryDivision(request);
            
            // 2. 填充基础参数
            fillQueryBaseRequest(request);
            
            // 3. 生成签名
            signService.signRequest(request);
            
            // 4. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "assureDivision/query";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("查询分账订单请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 5. 解析响应并验签
            QueryDivisionResponse response = JSONUtil.toBean(jsonResponse, QueryDivisionResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("查询分账订单响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 6. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("查询分账订单失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("查询分账订单失败: " + response.getMsg());
            }
            
            log.info("查询分账订单成功, assureOrderId:{}, state:{}", 
                    response.getAssureOrderId(), response.getState());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询分账订单异常", e);
            throw PaymentException.systemError("查询分账订单失败: " + e.getMessage());
        }
    }
    
    @Override
    public RefundAssureOrderResponse refundAssureOrder(RefundAssureOrderRequest request) {
        log.info("开始分账撤销, chanNo:{}, refundMchOrderNo:{}, refundAmount:{}", 
                request.getChanNo(), request.getRefundMchOrderNo(), request.getRefundAmount());
        
        try {
            // 1. 参数校验
            validateRefundAssureOrder(request);
            
            // 2. 解析渠道参数(如果存在)
            List<DivisionRefundInfoDTO> refundInfoList = parseChannelExtra(request.getChannelExtra());
            
            // 3. 根据渠道校验参数
            validateChannelRefundParams(request.getChanNo(), request, refundInfoList);
            
            // 4. 填充基础参数
            fillRefundBaseRequest(request);
            
            // 5. 生成签名
            signService.signRequest(request);
            
            // 6. 调用通企付API
            String apiUrl = paymentConfig.getApiUrl() + "assureDivision/refund";
            String jsonRequest = JSONUtil.toJsonStr(request);
            log.debug("分账撤销请求: {}, 参数: {}", apiUrl, jsonRequest);
            
            String jsonResponse = com.houjiang.payment.util.HttpUtil.post(
                apiUrl,
                jsonRequest,
                paymentConfig.getConnectTimeout(),
                paymentConfig.getReadTimeout()
            );
            
            // 7. 解析响应并验签
            RefundAssureOrderResponse response = JSONUtil.toBean(jsonResponse, RefundAssureOrderResponse.class);
            if (!signService.verifyResponse(response)) {
                log.error("分账撤销响应签名验证失败, response: {}", jsonResponse);
                throw PaymentException.bizError("响应签名验证失败");
            }
            
            // 8. 检查响应状态
            if (!"0".equals(response.getCode())) {
                log.error("分账撤销失败, code:{}, msg:{}", response.getCode(), response.getMsg());
                throw PaymentException.bizError("分账撤销失败: " + response.getMsg());
            }
            
            // TODO: 9. 保存撤销记录到数据库
            
            log.info("分账撤销成功, refundAssureOrderId:{}, state:{}", 
                    response.getRefundAssureOrderId(), response.getState());
            return response;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("分账撤销异常", e);
            throw PaymentException.systemError("分账撤销失败: " + e.getMessage());
        }
    }
    
    /** ===== 批量分账辅助方法 ===== */
    
    private void fillBaseRequest(BatchDivisionRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
    }
    
    private void validateBatchDivision(BatchDivisionRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("分账渠道不能为空");
        }
        if (StrUtil.isBlank(request.getBatchDivisionInfos())) {
            throw PaymentException.bizError("待分账列表不能为空");
        }
    }
    
    private List<BatchDivisionInfoDTO> parseBatchDivisionInfos(String batchDivisionInfosJson) {
        try {
            if (!JSONUtil.isTypeJSON(batchDivisionInfosJson)) {
                throw PaymentException.bizError("待分账列表必须是JSON格式");
            }
            
            JSONArray jsonArray = JSONUtil.parseArray(batchDivisionInfosJson);
            List<BatchDivisionInfoDTO> list = jsonArray.toList(BatchDivisionInfoDTO.class);
            
            if (list == null || list.isEmpty()) {
                throw PaymentException.bizError("待分账列表不能为空");
            }
            
            if (list.size() > 50) {
                throw PaymentException.bizError("待分账列表最多支持50条");
            }
            
            return list;
            
        } catch (Exception e) {
            log.error("解析待分账列表失败", e);
            throw PaymentException.bizError("待分账列表格式错误");
        }
    }
    
    private void validateDivisionList(String chanNo, List<BatchDivisionInfoDTO> divisionList) {
        if ((PaymentConstants.ChannelCode.ALLINPAY.equals(chanNo) 
                || PaymentConstants.ChannelCode.YUNST2YHZH.equals(chanNo)) 
                && divisionList.size() > 1) {
            throw PaymentException.bizError(chanNo + "渠道仅支持上送一条分账数据");
        }
    }
    
    private BatchDivisionResponse buildMockBatchResponse(BatchDivisionRequest request, 
                                                          List<BatchDivisionInfoDTO> divisionList) {
        BatchDivisionResponse response = new BatchDivisionResponse();
        response.setCode("0");
        response.setMsg("批量分账成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setBatchNo("BATCH" + System.currentTimeMillis());
        response.setState(PaymentConstants.DivisionStatus.PROCESSING);
        return response;
    }
    
    /** ===== 分账通知辅助方法 ===== */
    
    private Map<String, String> parseNotifyParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (StrUtil.isNotBlank(paramValue)) {
                params.put(paramName, paramValue);
            }
        }
        
        return params;
    }
    
    private DivisionNotifyDTO mapToDivisionNotify(Map<String, String> params) {
        DivisionNotifyDTO dto = new DivisionNotifyDTO();
        BeanUtil.fillBeanWithMap(params, dto, true);
        return dto;
    }
    
    private void validateDivisionNotify(DivisionNotifyDTO notifyDTO) {
        if (StrUtil.isBlank(notifyDTO.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(notifyDTO.getBatchNo())) {
            throw PaymentException.bizError("分账批次号不能为空");
        }
        if (notifyDTO.getState() == null) {
            throw PaymentException.bizError("分账状态不能为空");
        }
    }
    
    private boolean isDuplicateDivisionNotify(String assureOrderId, String mchAssureOrderNo) {
        // TODO: 从Redis缓存检查
        return false;
    }
    
    /** ===== 查询分账订单辅助方法 ===== */
    
    private void fillQueryBaseRequest(QueryDivisionRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
    }
    
    private void validateQueryDivision(QueryDivisionRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        
        if (StrUtil.isBlank(request.getMchAssureOrderNo()) 
                && StrUtil.isBlank(request.getAssureOrderId())) {
            throw PaymentException.bizError("商户分账订单号和通企付分账订单号至少填写一个");
        }
    }
    
    private QueryDivisionResponse buildMockQueryResponse(QueryDivisionRequest request) {
        QueryDivisionResponse response = new QueryDivisionResponse();
        response.setCode("0");
        response.setMsg("查询成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        
        response.setAssureOrderId(StrUtil.isNotBlank(request.getAssureOrderId()) 
                ? request.getAssureOrderId() : "ASS" + System.currentTimeMillis());
        response.setMchAssureOrderNo(StrUtil.isNotBlank(request.getMchAssureOrderNo()) 
                ? request.getMchAssureOrderNo() : "DIV" + System.currentTimeMillis());
        response.setPayOrderId("P" + System.currentTimeMillis());
        response.setMchOrderNo("MCH" + System.currentTimeMillis());
        response.setBatchNo("BATCH" + System.currentTimeMillis());
        response.setState(PaymentConstants.DivisionStatus.SUCCESS);
        response.setOrderAmount(100000L);
        response.setTotalOrderAmount(100000L);
        response.setCouponAmount(5000L);
        response.setCreatedAt(System.currentTimeMillis());
        response.setSuccessTime(System.currentTimeMillis());
        
        List<DivisionDetailDTO> details = new ArrayList<>();
        DivisionDetailDTO detail = new DivisionDetailDTO();
        detail.setDivisionBizMemNo("USER001");
        detail.setAmount(30000L);
        detail.setRemark("分账给商户A");
        details.add(detail);
        response.setDivisionDetails(details);
        
        return response;
    }
    
    /** ===== 分账撤销辅助方法 ===== */
    
    private void fillRefundBaseRequest(RefundAssureOrderRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            request.setMchNo(paymentConfig.getMerchantNo());
        }
        if (StrUtil.isBlank(request.getAppId())) {
            request.setAppId(paymentConfig.getAppId());
        }
        request.setReqTime(TimeUtil.getCurrentDateTimeStr());
        
        if (request.getReqSeqNo() == null) {
            request.setReqSeqNo("REQ" + System.currentTimeMillis());
        }
    }
    
    private void validateRefundAssureOrder(RefundAssureOrderRequest request) {
        if (StrUtil.isBlank(request.getMchNo())) {
            throw PaymentException.bizError("商户号不能为空");
        }
        if (StrUtil.isBlank(request.getAppId())) {
            throw PaymentException.bizError("应用ID不能为空");
        }
        if (StrUtil.isBlank(request.getChanNo())) {
            throw PaymentException.bizError("渠道编号不能为空");
        }
        if (StrUtil.isBlank(request.getRefundMchOrderNo())) {
            throw PaymentException.bizError("商户订单号不能为空");
        }
        if (request.getRefundAmount() == null || request.getRefundAmount() <= 0) {
            throw PaymentException.bizError("撤销金额必须大于0");
        }
        
        if (StrUtil.isBlank(request.getMchOrderNo()) 
                && StrUtil.isBlank(request.getAssureOrderId())) {
            throw PaymentException.bizError("原商户分账订单号和原通企付分账订单号至少填写一个");
        }
    }
    
    private List<DivisionRefundInfoDTO> parseChannelExtra(String channelExtra) {
        if (StrUtil.isBlank(channelExtra)) {
            return null;
        }
        
        try {
            if (!JSONUtil.isTypeJSON(channelExtra)) {
                throw PaymentException.bizError("渠道参数必须是JSON格式");
            }
            
            JSONArray jsonArray = JSONUtil.parseArray(channelExtra);
            return jsonArray.toList(DivisionRefundInfoDTO.class);
            
        } catch (Exception e) {
            log.error("解析渠道参数失败", e);
            throw PaymentException.bizError("渠道参数格式错误");
        }
    }
    
    private void validateChannelRefundParams(String chanNo, RefundAssureOrderRequest request, 
                                             List<DivisionRefundInfoDTO> refundInfoList) {
        
        if (PaymentConstants.ChannelCode.ALLINPAY.equals(chanNo)) {
            // allinpay渠道无需上送channelExtra
            if (StrUtil.isNotBlank(request.getChannelExtra())) {
                log.warn("allinpay渠道无需上送channelExtra参数");
            }
            
        } else if (PaymentConstants.ChannelCode.YUNST2ISV.equals(chanNo)) {
            // yunst2isv如不上送默认从收款人撤销
            if (refundInfoList != null && !refundInfoList.isEmpty()) {
                for (int i = 0; i < refundInfoList.size(); i++) {
                    DivisionRefundInfoDTO refundInfo = refundInfoList.get(i);
                    String prefix = "第" + (i + 1) + "条撤销信息: ";
                    
                    if (refundInfo.getRefundAmount() == null || refundInfo.getRefundAmount() <= 0) {
                        throw PaymentException.bizError(prefix + "撤销金额必须大于0");
                    }
                    if (StrUtil.isBlank(refundInfo.getDivisionBizMemNo())) {
                        throw PaymentException.bizError(prefix + "yunst2isv渠道divisionBizMemNo必填");
                    }
                }
            }
            
        } else if (PaymentConstants.ChannelCode.YUNST2YHZH.equals(chanNo)) {
            // yunst2yhzh必须上送channelExtra
            if (refundInfoList == null || refundInfoList.isEmpty()) {
                throw PaymentException.bizError("yunst2yhzh渠道channelExtra必填");
            }
            
            for (int i = 0; i < refundInfoList.size(); i++) {
                DivisionRefundInfoDTO refundInfo = refundInfoList.get(i);
                String prefix = "第" + (i + 1) + "条撤销信息: ";
                
                if (refundInfo.getRefundAmount() == null || refundInfo.getRefundAmount() <= 0) {
                    throw PaymentException.bizError(prefix + "撤销金额必须大于0");
                }
                
                if (refundInfo.getRefundInfoList() == null || refundInfo.getRefundInfoList().isEmpty()) {
                    throw PaymentException.bizError(prefix + "refundInfoList必填");
                }
                if (refundInfo.getRefundInfoList().size() > 1) {
                    throw PaymentException.bizError(prefix + "yunst2yhzh只允许上送一条refundInfoList");
                }
                
                RefundInfoDTO refundInfoDetail = refundInfo.getRefundInfoList().get(0);
                if (StrUtil.isBlank(refundInfoDetail.getDivisionBizMemNo())) {
                    throw PaymentException.bizError(prefix + "divisionBizMemNo必填");
                }
                if (refundInfoDetail.getRefundAmount() == null || refundInfoDetail.getRefundAmount() <= 0) {
                    throw PaymentException.bizError(prefix + "明细撤销金额必须大于0");
                }
            }
        }
    }
    
    private RefundAssureOrderResponse buildMockRefundResponse(RefundAssureOrderRequest request) {
        RefundAssureOrderResponse response = new RefundAssureOrderResponse();
        response.setCode("0");
        response.setMsg("分账撤销成功");
        response.setRespTime(TimeUtil.getCurrentDateTimeStr());
        response.setReqSeqNo(request.getReqSeqNo());
        response.setRefundAssureOrderId("REF" + System.currentTimeMillis());
        response.setState(PaymentConstants.RefundAssureStatus.SUCCESS);
        response.setAssureAmount(200000L);
        response.setRefundAmount(request.getRefundAmount());
        
        return response;
    }
    
    @Override
    public String handleRefundAssureNotify(HttpServletRequest httpRequest) {
        log.info("接收分账撤销结果通知");
        
        try {
            // 1. 获取所有请求参数
            Map<String, String> params = parseNotifyParams(httpRequest);
            
            // 2. 解析通知参数
            RefundAssureNotifyDTO notifyDTO = parseRefundAssureNotifyParams(params);
            
            log.info("分账撤销通知参数: refundAssureOrderId:{}, state:{}, successTime:{}",
                    notifyDTO.getRefundAssureOrderId(), notifyDTO.getState(), notifyDTO.getSuccessTime());
            
            // 3. 验签
            if (!signService.verifyNotify(params)) {
                log.error("分账撤销通知验签失败");
                return "fail";
            }
            
            // 4. 检查是否为重复通知
            if (isDuplicateRefundAssureNotify(notifyDTO.getRefundAssureOrderId())) {
                log.warn("分账撤销通知重复, refundAssureOrderId:{}", notifyDTO.getRefundAssureOrderId());
                // 重复通知也返回success
                return "success";
            }
            
            // TODO: 5. 查询本地分账撤销订单
            // RefundAssureOrder localOrder = refundAssureOrderMapper.selectByRefundAssureOrderId(
            //     notifyDTO.getRefundAssureOrderId());
            
            // TODO: 6. 更新分账撤销订单状态
            // localOrder.setState(Integer.valueOf(notifyDTO.getState()));
            // localOrder.setSuccessTime(new Date(notifyDTO.getSuccessTime()));
            // if (StrUtil.isNotBlank(notifyDTO.getErrMsg())) {
            //     localOrder.setErrMsg(notifyDTO.getErrMsg());
            // }
            // refundAssureOrderMapper.updateById(localOrder);
            
            // TODO: 7. 如果撤销成功,更新原分账订单状态
            // if ("2".equals(notifyDTO.getState())) {
            //     divisionOrderMapper.updateRefundStatus(
            //         notifyDTO.getAssureOrderId(), 
            //         PaymentConstants.RefundAssureStatus.SUCCESS);
            // }
            
            // TODO: 8. 记录通知日志
            // notifyLogMapper.insert(NotifyLog.builder()
            //     .notifyType("REFUND_ASSURE_NOTIFY")
            //     .refundAssureOrderId(notifyDTO.getRefundAssureOrderId())
            //     .state(notifyDTO.getState())
            //     .notifyTime(new Date())
            //     .notifyParams(JSONUtil.toJsonStr(params))
            //     .build());
            
            // TODO: 9. 缓存通知记录(防止重复通知)
            // redisTemplate.setex("refund_assure_notify:" + notifyDTO.getRefundAssureOrderId(), 
            //     86400, "1"); // 24小时
            
            // TODO: 10. 如果撤销成功,触发业务回调
            // if ("2".equals(notifyDTO.getState())) {
            //     callbackService.notifyMerchantRefundAssure(localOrder);
            // }
            
            log.info("分账撤销通知处理成功, refundAssureOrderId:{}, state:{}",
                    notifyDTO.getRefundAssureOrderId(), notifyDTO.getState());
            
            // 注意:必须返回小写的success,前后不能有空格和换行符
            return "success";
            
        } catch (Exception e) {
            log.error("分账撤销通知处理异常", e);
            return "fail";
        }
    }
    
    /**
     * 解析分账撤销通知参数
     */
    private RefundAssureNotifyDTO parseRefundAssureNotifyParams(Map<String, String> params) {
        RefundAssureNotifyDTO dto = new RefundAssureNotifyDTO();
        dto.setOrgId(params.get("orgId"));
        dto.setMchNo(params.get("mchNo"));
        dto.setAppId(params.get("appId"));
        dto.setSign(params.get("sign"));
        dto.setSignType(params.get("signType"));
        dto.setPayOrderId(params.get("payOrderId"));
        dto.setMchOrderNo(params.get("mchOrderNo"));
        dto.setAssureOrderId(params.get("assureOrderId"));
        dto.setRefundAssureMchOrderNo(params.get("refundAssureMchOrderNo"));
        dto.setRefundAssureOrderId(params.get("refundAssureOrderId"));
        dto.setAssureRefundAmount(params.get("assureRefundAmount"));
        dto.setState(params.get("state"));
        
        // 解析successTime(长整型)
        String successTimeStr = params.get("successTime");
        if (StrUtil.isNotBlank(successTimeStr)) {
            try {
                dto.setSuccessTime(Long.valueOf(successTimeStr));
            } catch (NumberFormatException e) {
                log.warn("解析successTime失败: {}", successTimeStr);
            }
        }
        
        dto.setErrMsg(params.get("errMsg"));
        dto.setAllParams(params);
        
        return dto;
    }
    
    /**
     * 检查是否为重复的分账撤销通知
     */
    private boolean isDuplicateRefundAssureNotify(String refundAssureOrderId) {
        // TODO: 从 Redis 或数据库检查是否已处理过该通知
        // String key = "refund_assure_notify:" + refundAssureOrderId;
        // return redisTemplate.exists(key);
        
        // 暂时返回false,实际项目中应使用Redis缓存检查
        return false;
    }
}
