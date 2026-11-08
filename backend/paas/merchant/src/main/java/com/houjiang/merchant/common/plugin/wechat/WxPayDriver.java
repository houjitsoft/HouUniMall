package com.houjiang.merchant.common.plugin.wechat;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.binarywang.wxpay.bean.merchanttransfer.DetailsQueryResult;
import com.github.binarywang.wxpay.bean.merchanttransfer.MerchantDetailsQueryRequest;
import com.github.binarywang.wxpay.bean.merchanttransfer.TransferCreateResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.houjiang.merchant.common.entity.setting.MallPay;
import com.houjiang.merchant.common.enums.ClientEnum;
import com.houjiang.merchant.common.mapper.setting.MallPayMapper;
import com.houjiang.merchant.common.plugin.wechat.enums.WxTransferFailReason;
import com.houjiang.merchant.common.plugin.wechat.request.*;
import com.houjiang.merchant.common.plugin.wechat.response.TransferCreateResultWrapper;
import com.houjiang.merchant.common.plugin.wechat.response.TransferQueryResponse;
import com.houjiang.merchant.common.plugin.wechat.response.TransferToUserResponse;
import com.houjiang.merchant.common.util.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 微信支付驱动
 */
@Component
public class WxPayDriver {

    private static MallPayMapper mallPayMapper;

    private static WxPayService wxPayService;
    private static WxPayService wxPayMnpService;

    /**
     * 注入支付配置依赖
     */
    @Resource
    public void setMallPayMapper(MallPayMapper mallPayMapper) {
        WxPayDriver.mallPayMapper = mallPayMapper;
    }

    /**
     * 注入微信支付依赖
     */
    @Resource
    public void setWxPayService(WxPayService wxPayService) {
        WxPayDriver.wxPayService = wxPayService;
    }

    @Autowired
    public void setWxPayMnpService(WxPayService wxPayMnpService) {
        WxPayDriver.wxPayMnpService = wxPayMnpService;
    }

    /**
     * 微信统一下单
     *
     * @param requestV3 请求参数
     * @return WxPayUnifiedOrderV3Result.JsapiResult
     * @throws Exception 异常
     */
    public static Object unifiedOrder(PaymentRequestV3 requestV3) throws Exception {
        // 订单参数
        Integer terminal       = requestV3.getTerminal();
        String openId          = requestV3.getOpenId();
        String attach          = requestV3.getAttach();
        String outTradeNo      = requestV3.getOutTradeNo();
        BigDecimal orderAmount = requestV3.getOrderAmount();
        String description     = requestV3.getDescription();

        // 失效时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Long expireTime = System.currentTimeMillis() + (30 * 60 * 1000);
        String timeExpire = format.format(expireTime) + "+08:00";

        // 订单信息
        WxPayUnifiedOrderV3Request wxPayUnifiedOrderV3Request = new WxPayUnifiedOrderV3Request();
        wxPayUnifiedOrderV3Request.setOutTradeNo(outTradeNo);
        wxPayUnifiedOrderV3Request.setDescription(description);
        wxPayUnifiedOrderV3Request.setTimeExpire(timeExpire);
        wxPayUnifiedOrderV3Request.setAttach(attach);
        wxPayUnifiedOrderV3Request.setNotifyUrl(RequestUtils.uri() + "/api/mall.pay/notifyMnp");
        //wxPayUnifiedOrderV3Request.setNotifyUrl("http://v36a9498.natappfree.cc/api/mall.pay/notifyMnp");

        // 订单金额
        WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
        amount.setTotal(AmountUtil.yuan2Fen(orderAmount.toPlainString()));
        amount.setCurrency("CNY");
        wxPayUnifiedOrderV3Request.setAmount(amount);

        // 付款人员
        WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
        payer.setOpenid(openId);

        // H5平台
        TradeTypeEnum tradeTypeEnum = TradeTypeEnum.JSAPI;

        if (terminal == ClientEnum.H5.getCode()) {
            tradeTypeEnum = TradeTypeEnum.H5;
            WxPayUnifiedOrderV3Request.SceneInfo sceneInfo = new WxPayUnifiedOrderV3Request.SceneInfo();
            WxPayUnifiedOrderV3Request.H5Info h5Info = new WxPayUnifiedOrderV3Request.H5Info();
            h5Info.setType(RequestUtils.device());
            sceneInfo.setH5Info(h5Info);
            sceneInfo.setPayerClientIp(IpUtils.getHostIp());
            wxPayUnifiedOrderV3Request.setSceneInfo(sceneInfo);
        }

        // PC平台
        if (terminal == ClientEnum.PC.getCode()) {
            payer.setOpenid(null);
            tradeTypeEnum = TradeTypeEnum.NATIVE;
        }

        // 发起订单
        WxPayService wxPayService = WxPayDriver.handler(terminal);
        wxPayUnifiedOrderV3Request.setPayer(payer);
        return wxPayService.createOrderV3(tradeTypeEnum, wxPayUnifiedOrderV3Request);
    }

    /**
     * 微信支付-申请退款
     * 文档地址: https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_4
     * 接口链接: https://api.mch.weixin.qq.com/secapi/pay/refund
     *
     * @param request 请求参数
     * @return WxPayRefundV3Result 退款结果
     * @throws WxPayException 退款异常
     */
    public static WxPayRefundV3Result refund(RefundRequestV3 request) throws WxPayException {
        WxPayRefundV3Request requestObj = new WxPayRefundV3Request();
        requestObj.setTransactionId(request.getTransactionId());
        requestObj.setOutTradeNo(request.getOutTradeNo());
        requestObj.setOutRefundNo(request.getOutRefundNo());
        requestObj.setReason(request.getReason());
        requestObj.setNotifyUrl(request.getNotifyUrl());
        requestObj.setSubMchid(request.getSubMchid());
        requestObj.setGoodsDetails(request.getGoodsDetails());
        WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
        amount.setRefund(request.getRefundAmount());
        amount.setTotal(request.getTotalAmount());
        amount.setCurrency(StringUtils.isEmpty(request.getCurrency()) ? "CNY" : request.getCurrency());
        requestObj.setAmount(amount);

        WxPayService wxPayService = WxPayDriver.handler(4);
        return wxPayService.refundV3(requestObj);
    }

    /**
     * 对象句柄
     *
     * @author Amos
     * @return WxPayService
     */
    public static WxPayService handler(Integer terminal) {
        if (ClientEnum.MNP.getCode() == terminal) {
            resetConfig("mnp");
            return wxPayMnpService;
        } else {
            resetConfig("oa");
            return wxPayService;
        }
    }

    /**
     * 重设配置
     *
     * @author Amos
     * @param type 类型
     */
    private static void resetConfig(String type) {
        MallPay config = mallPayMapper.selectOne(
                new QueryWrapper<MallPay>()
                        .eq("pay_type", 2)
                        .last("limit 1"));
        String scene = type.equals("oa") ? "oa_setting" : "mnp_setting";
        String appId = ConfigUtils.get(scene, "app_id", "");
        Map<String, String> params = MapUtils.jsonToMap(config.getConfig().toString());
        String mchId = params.get("wechat_mch_id");
        String paySignKey  = params.get("wechat_pay_sign_key");
        byte[] privateCert = params.getOrDefault("wechat_apiclient_cert", "").getBytes();
        byte[] privateKey  = params.getOrDefault("wechat_apiclient_key", "").getBytes();

        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(appId);
        payConfig.setMchId(mchId);
        payConfig.setApiV3Key(paySignKey);
        payConfig.setPrivateKeyContent(privateKey);
        payConfig.setPrivateCertContent(privateCert);
        payConfig.setUseSandboxEnv(false);
        payConfig.setCertSerialNo(WXPayUtility.getCertificateSerial(params.getOrDefault("wechat_apiclient_cert", "")));
        payConfig.setPrivateKey(WXPayUtility.loadPrivateKeyFromString(params.getOrDefault("wechat_apiclient_key", "")));
        if (StringUtils.isNotEmpty(params.getOrDefault("wechat_public_serial", ""))) {
            payConfig.setPublicKeyId(params.getOrDefault("wechat_public_serial", ""));
        }
        if (StringUtils.isNotEmpty(params.getOrDefault("wechat_public_cert", ""))) {
            payConfig.setPublicKeyContent(params.getOrDefault("wechat_public_cert", "").getBytes());
        }
        if (!type.equals("oa")) {
            wxPayMnpService.setConfig(payConfig);
        } else {
            wxPayService.setConfig(payConfig);
        }
    }

    /**
     * 微信支付-商家转账到零钱（新接口V3）
     * 文档地址: https://pay.weixin.qq.com/doc/v3/merchant/4012716434
     * 接口链接: https://api.mch.weixin.qq.com/v3/fund-app/mch-transfer/transfer-bills
     *
     * @author Amos
     * @param request TransferRequest
     * @return TransferCreateResult
     */
    public static TransferCreateResultWrapper createTransferV3(TransferRequest request) throws WxPayException {
        Integer terminal = request.getTerminal();
        WxPayService wxPayService = WxPayDriver.handler(terminal);

        if (request.getDetailList() == null || request.getDetailList().isEmpty()) {
            throw new WxPayException("转账明细不能为空");
        }

        // 新接口是单笔转账，取第一笔明细
        TransferRequest.detailList detail = request.getDetailList().get(0);
        try {
            System.out.println(wxPayService);
            System.out.println(detail);
            return executeNewTransferApi(wxPayService, detail);
        } catch (Exception e) {
            throw new WxPayException(e.getMessage(), e);
        }
    }

    /**
     * 执行新的转账API调用
     */
    private static TransferCreateResultWrapper executeNewTransferApi(WxPayService wxPayService, TransferRequest.detailList detail) throws Exception {
        String host = "https://api.mch.weixin.qq.com";
        String path = "/v3/fund-app/mch-transfer/transfer-bills";
        String method = "POST";

        WxPayConfig config = wxPayService.getConfig();
        // 构建请求体
        TransferToUserRequest transferRequest = buildTransferRequest(config, detail);
        transferRequest.setNotifyUrl(RequestUtils.uri() + "/api/wx/withdrawNotify");
        String requestBody = WXPayUtility.toJson(transferRequest);

        // 打印请求参数
        System.out.println("========== 微信转账请求参数 ==========");
        System.out.println("请求URL: " + host + path);
        System.out.println("请求方法: " + method);
        System.out.println("请求体: " + requestBody);
        System.out.println("商户号: " + config.getMchId());
        System.out.println("证书序列号: " + config.getCertSerialNo());
        System.out.println("公钥ID: " + config.getPublicKeyId());
        System.out.println("应用ID: " + config.getAppId());
        System.out.println("=====================================");

        // 构建HTTP请求
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request.Builder reqBuilder = new Request.Builder().url(host + path);
        reqBuilder.addHeader("Accept", "application/json");
        reqBuilder.addHeader("Content-Type", "application/json");
        reqBuilder.addHeader("Wechatpay-Serial", config.getPublicKeyId());
        // 生成Authorization头
        String authorization = WXPayUtility.buildAuthorization(
                config.getMchId(),
                config.getCertSerialNo(),
                config.getPrivateKey(),
                method,
                path,
                requestBody
        );
        reqBuilder.addHeader("Authorization", authorization);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody);
        reqBuilder.method(method, body);
        Request httpRequest = reqBuilder.build();
        // 发送HTTP请求
        try (Response httpResponse = client.newCall(httpRequest).execute()) {
            String responseBody = WXPayUtility.extractBody(httpResponse);
            if (httpResponse.code() >= 200 && httpResponse.code() < 300) {
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                if (StringUtils.isNotNull(jsonObject.getString("code"))) {
                    throw new WxPayException(jsonObject.getString("message"));
                }
                // 解析响应并转换为TransferCreateResult
                TransferToUserResponse response = WXPayUtility.fromJson(responseBody, TransferToUserResponse.class);
                TransferCreateResult transferCreateResult = convertToTransferCreateResult(response, detail);

                // 返回包装类，包含原有结果和完整的响应JSON
                return new TransferCreateResultWrapper(transferCreateResult, responseBody);
            } else {
                throw new WXPayUtility.ApiException(httpResponse.code(), responseBody, httpResponse.headers());
            }
        } catch (IOException e) {
            throw new WxPayException(e.getMessage(), e);
        }
    }

    /**
     * 构建转账请求对象
     */
    private static TransferToUserRequest buildTransferRequest(WxPayConfig config, TransferRequest.detailList detail) {
        TransferToUserRequest request = new TransferToUserRequest();
        request.setAppid(config.getAppId());
        request.setOutBillNo(detail.getOutDetailNo());
        //request.setTransferSceneId("1005"); // 现金营销场景
        // 现金营销场景
        request.setTransferSceneId("1000");
        request.setOpenid(detail.getOpenid());
        request.setTransferAmount(Long.valueOf(detail.getTransferAmount()));
        request.setTransferRemark(detail.getTransferRemark());

        // 收款用户姓名（转账金额>=2000元时必填）
        if (StringUtils.isNotEmpty(detail.getUserName())) {
            // 如果有公钥，需要加密用户姓名
            if (config.getPublicKeyContent() != null && config.getPublicKeyContent().length > 0) {
                PublicKey publicKey = WXPayUtility.loadPublicKeyFromString(new String(config.getPublicKeyContent()));
                request.setUserName(WXPayUtility.encrypt(publicKey, detail.getUserName()));
            } else {
                request.setUserName(detail.getUserName());
            }
        }

        // 转账场景报备信息
        List<TransferSceneReportInfo> reportInfos = new ArrayList<>();
        //reportInfos.add(new TransferSceneReportInfo("岗位类型", "销售员"));
        //reportInfos.add(new TransferSceneReportInfo("报酬说明", "用户提现"));
        reportInfos.add(new TransferSceneReportInfo("活动名称", "销售员"));
        reportInfos.add(new TransferSceneReportInfo("奖励说明", "用户提现"));
        request.setTransferSceneReportInfos(reportInfos);

        return request;
    }

    /**
     * 转换响应为TransferCreateResult
     */
    private static TransferCreateResult convertToTransferCreateResult(TransferToUserResponse response, TransferRequest.detailList detail) {
        TransferCreateResult result = new TransferCreateResult();
        result.setOutBatchNo(detail.getOutDetailNo());
        result.setBatchId(response.getTransferBillNo());
        result.setCreateTime(response.getCreateTime());

        // 映射状态
        if (response.getState() != null) {
            switch (response.getState()) {
                case ACCEPTED:
                case PROCESSING:
                    result.setBatchStatus("ACCEPTED");
                    break;
                case SUCCESS:
                    result.setBatchStatus("SUCCESS");
                    break;
                case FAIL:
                    result.setBatchStatus("FAIL");
                    break;
                default:
                    result.setBatchStatus("PROCESSING");
                    break;
            }
        }

        return result;
    }

    /**
     * 微信支付-查询转账明细（新接口V3）
     * 文档地址: https://pay.weixin.qq.com/doc/v3/merchant/4012716434
     * 接口链接: https://api.mch.weixin.qq.com/v3/fund-app/mch-transfer/transfer-bills/out-bill-no/{out_bill_no}
     *
     * @author Amos
     * @param request TransferQueryRequest
     * @return DetailsQueryResult
     */
    public static DetailsQueryResult transferQueryV3(TransferQueryRequest request) throws WxPayException {
        Integer terminal = request.getTerminal();
        WxPayService wxPayService = WxPayDriver.handler(terminal);

        try {
            return executeNewTransferQueryApi(wxPayService, request.getOutDetailNo());
        } catch (Exception e) {
            throw new WxPayException(e.getMessage(), e);
        }
    }

    /**
     * 执行新的转账查询API调用
     */
    private static DetailsQueryResult executeNewTransferQueryApi(WxPayService wxPayService, String outBillNo) throws Exception {
        String host = "https://api.mch.weixin.qq.com";
        String path = "/v3/fund-app/mch-transfer/transfer-bills/out-bill-no/" + outBillNo;
        String method = "GET";
        String requestBody = "";

        WxPayConfig config = wxPayService.getConfig();

        // 构建HTTP请求
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request.Builder reqBuilder = new Request.Builder().url(host + path);
        reqBuilder.addHeader("Accept", "application/json");

        // 如果有微信支付公钥ID，添加Wechatpay-Serial头
        if (StringUtils.isNotEmpty(config.getPublicKeyId())) {
            reqBuilder.addHeader("Wechatpay-Serial", config.getPublicKeyId());
        }

        // 生成Authorization头
        String authorization = WXPayUtility.buildAuthorization(
                config.getMchId(),
                config.getCertSerialNo(),
                config.getPrivateKey(),
                method,
                path,
                requestBody
        );
        reqBuilder.addHeader("Authorization", authorization);

        reqBuilder.get();
        Request httpRequest = reqBuilder.build();

        // 发送HTTP请求
        try (Response httpResponse = client.newCall(httpRequest).execute()) {
            String responseBody = WXPayUtility.extractBody(httpResponse);

            if (httpResponse.code() >= 200 && httpResponse.code() < 300) {
                // 解析响应并转换为DetailsQueryResult
                TransferQueryResponse response = WXPayUtility.fromJson(responseBody, TransferQueryResponse.class);
                return convertToDetailsQueryResult(response);
            } else {
                throw new WXPayUtility.ApiException(httpResponse.code(), responseBody, httpResponse.headers());
            }
        } catch (IOException e) {
            throw new WxPayException("发送转账查询请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换查询响应为DetailsQueryResult
     */
    private static DetailsQueryResult convertToDetailsQueryResult(TransferQueryResponse response) {
        DetailsQueryResult result = new DetailsQueryResult();
        result.setOutDetailNo(response.getOutBillNo());
        result.setDetailId(response.getTransferBillNo());
        result.setTransferAmount(response.getTransferAmount() != null ? response.getTransferAmount().intValue() : null);
        result.setTransferRemark(response.getTransferRemark());
        result.setOpenid(response.getOpenid());
        result.setUserName(response.getUserName());
        result.setInitiateTime(response.getCreateTime());
        result.setUpdateTime(response.getUpdateTime());

        // 映射状态
        if (response.getState() != null) {
            switch (response.getState()) {
                case ACCEPTED:
                    result.setDetailStatus("ACCEPTED");
                    break;
                case PROCESSING:
                    result.setDetailStatus("PROCESSING");
                    break;
                case SUCCESS:
                    result.setDetailStatus("SUCCESS");
                    break;
                case WAIT_USER_CONFIRM:
                    result.setDetailStatus("WAIT_USER_CONFIRM");
                    break;
                case TRANSFERING:
                    result.setDetailStatus("TRANSFERING");
                    break;
                case CANCELING:
                    result.setDetailStatus("CANCELING");
                    break;
                case CANCELLED:
                    result.setDetailStatus("CANCELLED");
                    break;
                case FAIL:
                    result.setDetailStatus("FAIL");
                    // 设置失败原因的中文描述
                    if (response.getFailReason() != null) {
                        String failReasonMessage = WxTransferFailReason.getMessageByCode(response.getFailReason());
                        result.setFailReason(failReasonMessage);
                    }
                    break;
                default:
                    result.setDetailStatus("PROCESSING");
                    break;
            }
        }

        return result;
    }

    /**
     * 商家明细单号查询转账明细单
     *
     * @author Amos
     * @param request TransferQueryRequest
     * @return DetailsQueryResult
     */
    public static DetailsQueryResult transferQuery(TransferQueryRequest request) throws WxPayException {
        Integer terminal = request.getTerminal();
        //WxPayService wxPayService = WxPayDriver.handler(terminal);
        MerchantDetailsQueryRequest queryRequest = new MerchantDetailsQueryRequest();
        queryRequest.setOutDetailNo(request.getOutDetailNo());
        queryRequest.setOutBatchNo(request.getOutBatchNo());
        TransferQueryRequest transferQueryRequest = new TransferQueryRequest();
        transferQueryRequest.setOutDetailNo(request.getOutDetailNo());
        transferQueryRequest.setOutBatchNo(request.getOutBatchNo());
        transferQueryRequest.setTerminal( terminal);
        DetailsQueryResult queryResult = WxPayDriver.transferQueryV3(transferQueryRequest);
        return queryResult;
    }
}
