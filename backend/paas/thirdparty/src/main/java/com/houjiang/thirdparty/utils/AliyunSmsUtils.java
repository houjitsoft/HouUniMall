package com.houjiang.thirdparty.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.alibaba.fastjson.JSON;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.exception.GlobalExceptionEnum;
import com.houjiang.thirdparty.constants.TemplateIdConstant;
import com.houjiang.thirdparty.dto.TencentSmsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 阿里云短信工具类：使用阿里云 dysmsapi SDK 发送短信。
 *
 * @author Amos
 */
public final class AliyunSmsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AliyunSmsUtils.class);

    private AliyunSmsUtils() {
    }

    /**
     * 发送短信（阿里云短信服务）。
     *
     * @param param 短信参数（手机号列表、模板ID、模板变量列表）
     */
    public static void testSms(TencentSmsDTO param) {
        // 参数校验
        if (TemplateIdConstant.SECRET_ID == null || TemplateIdConstant.SECRET_ID.trim().isEmpty()) {
            LOG.error("阿里云短信配置错误：SECRET_ID（AccessKeyId）为空");
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少AccessKeyId");
        }
        if (TemplateIdConstant.SECRET_KEY == null || TemplateIdConstant.SECRET_KEY.trim().isEmpty()) {
            LOG.error("阿里云短信配置错误：SECRET_KEY（AccessKeySecret）为空");
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少AccessKeySecret");
        }
        if (TemplateIdConstant.SIGN == null || TemplateIdConstant.SIGN.trim().isEmpty()) {
            LOG.error("阿里云短信配置错误：SIGN（签名）为空");
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少短信签名");
        }
        if (param.getTemplateID() == null || param.getTemplateID().trim().isEmpty()) {
            LOG.error("阿里云短信配置错误：模板ID为空");
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少模板ID");
        }
        if (param.getPhoneNumberSet() == null || param.getPhoneNumberSet().isEmpty()) {
            LOG.error("阿里云短信配置错误：手机号为空");
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少手机号");
        }

        String region = (TemplateIdConstant.REGION != null && !TemplateIdConstant.REGION.trim().isEmpty())
                ? TemplateIdConstant.REGION.trim() : "cn-hangzhou";

        LOG.info("=== 阿里云短信发送开始 ===");
        LOG.info("手机号: {}, 签名: {}, 模板ID: {}, Region: {}",
                param.getPhoneNumberSet().get(0), TemplateIdConstant.SIGN, param.getTemplateID(), region);

        try {
            IClientProfile profile = DefaultProfile.getProfile(region,
                    TemplateIdConstant.SECRET_ID, TemplateIdConstant.SECRET_KEY);
            DefaultProfile.addEndpoint(region, "Dysmsapi", "dysmsapi.aliyuncs.com");
            IAcsClient acsClient = new DefaultAcsClient(profile);

            // 构建手机号（去除区号 +86 前缀）
            String phone = param.getPhoneNumberSet().get(0);
            if (phone.startsWith("+86")) {
                phone = phone.substring(3);
            } else if (phone.startsWith("86") && phone.length() == 13) {
                phone = phone.substring(2);
            }

            // 构建模板参数 JSON，格式：{"code":"123456"}
            String templateParam = buildTemplateParam(param.getTemplateParamSet());

            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(TemplateIdConstant.SIGN);
            request.setTemplateCode(param.getTemplateID());
            request.setTemplateParam(templateParam);

            LOG.info("=== 阿里云短信请求参数: phone={}, sign={}, templateCode={}, templateParam={}",
                    phone, TemplateIdConstant.SIGN, param.getTemplateID(), templateParam);

            SendSmsResponse response = acsClient.getAcsResponse(request);

            LOG.info("=== 阿里云短信响应: Code={}, Message={}, RequestId={}, BizId={}",
                    response.getCode(), response.getMessage(), response.getRequestId(), response.getBizId());

            if (!"OK".equalsIgnoreCase(response.getCode())) {
                String errMsg = String.format("阿里云短信发送失败: Code=%s, Message=%s", response.getCode(), response.getMessage());
                LOG.error("=== {}", errMsg);
                // 流控等错误转为用户可读文案，避免原始报错暴露后用户反复点击加剧流控
                throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(),
                        toFriendlySmsError(response.getCode(), response.getMessage()));
            }

            LOG.info("=== 阿里云短信发送成功 ===");

        } catch (ClientException e) {
            String errMsg = String.format("阿里云短信发送异常: ErrCode=%s, ErrMsg=%s, RequestId=%s",
                    e.getErrCode(), e.getErrMsg(), e.getRequestId());
            LOG.error("=== {}", errMsg, e);
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(),
                    "短信发送失败: " + e.getErrMsg() + " (ErrorCode: " + e.getErrCode() + ")");
        } catch (DefinedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("=== 阿里云短信发送未知异常: {}", e.getMessage(), e);
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(),
                    "短信发送失败: " + e.getMessage());
        }
    }

    /**
     * 将阿里云短信错误码转换为用户友好文案。
     * 流控类错误（isv.BUSINESS_LIMIT_CONTROL）若原样暴露给用户，用户会因不理解原因而
     * 反复点击发送，进一步耗尽号码级流控额度，形成“收不到短信→反复点击→更收不到”的恶性循环。
     *
     * @param code    阿里云返回的错误码（如 isv.BUSINESS_LIMIT_CONTROL）
     * @param message 阿里云返回的原始错误信息（含“天级流控”“分钟级流控”等描述）
     * @return 用户可读的错误文案；无法识别的错误码保留原始信息便于排查
     */
    private static String toFriendlySmsError(String code, String message) {
        String raw = "短信发送失败: " + message + " (Code: " + code + ")";
        if (code == null) {
            return raw;
        }
        String msg = message == null ? "" : message;
        if ("isv.BUSINESS_LIMIT_CONTROL".equals(code)) {
            if (msg.contains("天级流控")) {
                return "该手机号今日验证码发送次数已达上限，请明天再试";
            }
            return "验证码发送过于频繁，请稍后再试";
        }
        if ("isv.MOBILE_NUMBER_ILLEGAL".equals(code)) {
            return "手机号格式不正确，请检查后重试";
        }
        if ("isv.BLACK_KEY_CONTROL".equals(code)) {
            return "该手机号暂不支持接收验证码，请联系客服";
        }
        return raw;
    }

    /**
     * 查询短信发送回执（阿里云 QuerySendDetails）。
     * SendSms 接口同步返回 OK 仅代表阿里云受理，真实投递结果以运营商回执为准，
     * 出现“接口成功但收不到短信”时必须用本方法查询回执定位根因。
     * 安全约定：返回结果不包含短信内容（Content）与 BizId，避免验证码泄露。
     *
     * @param phone    接收短信的手机号（11位，不带区号）
     * @param sendDate 发送日期，格式 yyyyMMdd，为空时查当天（阿里云仅支持近30天）
     * @return 回执明细列表，每条含 sendDate/sendStatusText/errCode/errMsg 字段
     */
    public static List<Map<String, Object>> queryDeliveryDetails(String phone, String sendDate) {
        if (TemplateIdConstant.SECRET_ID == null || TemplateIdConstant.SECRET_ID.trim().isEmpty()
                || TemplateIdConstant.SECRET_KEY == null || TemplateIdConstant.SECRET_KEY.trim().isEmpty()) {
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "短信配置错误：缺少阿里云凭证");
        }
        String region = (TemplateIdConstant.REGION != null && !TemplateIdConstant.REGION.trim().isEmpty())
                ? TemplateIdConstant.REGION.trim() : "cn-hangzhou";
        try {
            IClientProfile profile = DefaultProfile.getProfile(region,
                    TemplateIdConstant.SECRET_ID, TemplateIdConstant.SECRET_KEY);
            DefaultProfile.addEndpoint(region, "Dysmsapi", "dysmsapi.aliyuncs.com");
            IAcsClient acsClient = new DefaultAcsClient(profile);

            QuerySendDetailsRequest request = new QuerySendDetailsRequest();
            request.setPhoneNumber(phone);
            request.setSendDate(sendDate);
            request.setPageSize(10L);
            request.setCurrentPage(1L);

            QuerySendDetailsResponse response = acsClient.getAcsResponse(request);
            if (!"OK".equalsIgnoreCase(response.getCode())) {
                throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(),
                        "查询短信回执失败: " + response.getMessage() + " (Code: " + response.getCode() + ")");
            }

            List<Map<String, Object>> result = new ArrayList<>();
            if (response.getSmsSendDetailDTOs() != null) {
                for (QuerySendDetailsResponse.SmsSendDetailDTO dto : response.getSmsSendDetailDTOs()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("sendDate", dto.getSendDate());
                    item.put("sendStatusText", toDeliveryStatusText(dto.getSendStatus()));
                    item.put("errCode", dto.getErrCode());
                    item.put("errMsg", toFriendlyDeliveryError(dto.getSendStatus(), dto.getErrCode()));
                    result.add(item);
                }
            }
            return result;
        } catch (DefinedException e) {
            throw e;
        } catch (ClientException e) {
            LOG.error("=== 查询短信回执异常: ErrCode={}, ErrMsg={}", e.getErrCode(), e.getErrMsg(), e);
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(),
                    "查询短信回执失败: " + e.getErrMsg() + " (ErrorCode: " + e.getErrCode() + ")");
        } catch (Exception e) {
            LOG.error("=== 查询短信回执未知异常: {}", e.getMessage(), e);
            throw new DefinedException(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(),
                    "查询短信回执失败: " + e.getMessage());
        }
    }

    /**
     * 将阿里云回执 SendStatus 转为用户可读文案。
     *
     * @param sendStatus 回执状态（1=等待回执, 2=发送失败, 3=发送成功）
     * @return 状态文案
     */
    private static String toDeliveryStatusText(Long sendStatus) {
        if (sendStatus == null) {
            return "未知";
        }
        switch (sendStatus.intValue()) {
            case 1:
                return "等待运营商回执";
            case 2:
                return "发送失败";
            case 3:
                return "发送成功";
            default:
                return "未知";
        }
    }

    /**
     * 将阿里云回执错误码转换为用户友好文案。
     * PORT_NOT_REGISTERED 是工信部实名制报备新规下最常见的“接口成功但收不到短信”根因：
     * 签名子端口号未完成运营商侧企业实名制报备，运营商会直接拒收。
     *
     * @param errCode 回执错误码（如 PORT_NOT_REGISTERED），可能为 null（发送成功时）
     * @return 用户可读的错误说明；发送成功或无法识别时返回对应文案
     */
    private static String toFriendlyDeliveryError(Long sendStatus, String errCode) {
        // 阿里云 SendStatus=1 表示仍在等待运营商回执；errCode 为空并不代表已送达。
        if (sendStatus != null && sendStatus == 1L && (errCode == null || errCode.trim().isEmpty())) {
            return "等待运营商回执，暂未确认送达";
        }
        if (sendStatus != null && sendStatus == 3L
                && (errCode == null || errCode.trim().isEmpty() || "DELIVERED".equalsIgnoreCase(errCode))) {
            return "短信已成功送达";
        }
        if (errCode == null || errCode.trim().isEmpty()) {
            return "未返回运营商错误码";
        }
        switch (errCode) {
            case "PORT_NOT_REGISTERED":
                return "短信签名端口未完成运营商实名制报备，短信被运营商拒收，请联系平台管理员在阿里云完成签名报备";
            case "INVALID_NUMBER":
                return "手机号无效（空号或格式错误）";
            case "BLACK_KEY_CONTROL":
                return "该手机号在运营商黑名单中，无法接收短信";
            case "MOBILE_NOT_ON_SERVICE":
                return "手机号停机或不在服务区";
            case "MOBILE_ACCOUNT_ABORMAL":
                return "手机号账户异常，无法接收短信";
            case "SP_UNKNOWN_ERROR":
                return "运营商处理异常，请稍后重试";
            default:
                return "运营商拒收短信，错误码: " + errCode;
        }
    }

    /**
     * 将模板变量列表构建为阿里云短信模板参数 JSON。
     * 阿里云模板参数格式：{"code":"123456"} 或 {"name":"xxx","code":"yyy"}
     *
     * @param templateParamSet 模板变量列表（顺序对应模板占位符 ${code}、${name} 等）
     * @return JSON 字符串，若无变量则返回 null
     */
    private static String buildTemplateParam(List<String> templateParamSet) {
        if (templateParamSet == null || templateParamSet.isEmpty()) {
            return null;
        }
        // 常见验证码模板只有一个变量 code
        if (templateParamSet.size() == 1) {
            return "{\"code\":\"" + templateParamSet.get(0) + "\"}";
        }
        // 多变量时按 param1, param2, ... 命名
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        String[] keys = {"code", "name", "param1", "param2", "param3", "param4"};
        for (int i = 0; i < templateParamSet.size(); i++) {
            String key = i < keys.length ? keys[i] : ("param" + (i + 1));
            map.put(key, templateParamSet.get(i));
        }
        return JSON.toJSONString(map);
    }
}
