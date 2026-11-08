package com.houjiang.thirdparty.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.exception.GlobalExceptionEnum;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.secverifyapiutil.MessageMD5Util;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.thirdparty.constants.TemplateIdConstant;
import com.houjiang.thirdparty.dto.TencentSmsDTO;
import com.houjiang.thirdparty.enums.SmsStrategy;
import com.houjiang.thirdparty.feign.BaseServiceClient;
import com.houjiang.thirdparty.feign.RiderServiceClient;
import com.houjiang.thirdparty.feign.UserServiceClient;
import com.houjiang.thirdparty.service.SendSmsService;
import com.houjiang.thirdparty.utils.AliyunSmsUtils;
import com.houjiang.thirdparty.utils.MailUtil;
import com.houjiang.thirdparty.utils.TencentSmsUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Amos
 */
@Slf4j
@Service
public class SendSmsServiceImpl implements SendSmsService {
    /** 启用中的短信通道类型缓存（避免每次发码都同步打 base） */
    private static final String SMS_ACTIVE_TYPE_CACHE_KEY = "SMS_ACTIVE_TYPE";
    /** 验证码有效期（分钟）缓存 */
    private static final String SMS_CODE_EXPIRE_CACHE_KEY = "SMS_CODE_EXPIRE_MINUTES";
    private static final int SMS_META_CACHE_SECONDS = 600;

    private long midTime = 60;
    private long endTime;
    private long currentTime;
    private boolean enable = true;



    @Resource
    private Map<String, SmsStrategy> strategies;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private RiderServiceClient riderServiceClient;

    @Autowired
    private BaseServiceClient baseServiceClient;
    
    @Autowired
    private com.houjiang.thirdparty.service.message.MessageTemplateService messageTemplateService;
    
    @Autowired(required = false)
    private com.houjiang.thirdparty.service.message.MessageRecordService messageRecordService;

    /**
     * 获取默认短信配置
     * 注意：此配置仅用于开发测试，生产环境必须配置真实的短信服务商信息
     */
    private Map<String, Object> getDefaultSmsConfig() {
        Map<String, Object> defaultConfig = new HashMap<>();
        // 注意：这些是无效的默认值，仅用于避免空指针异常
        // 生产环境必须配置真实的短信服务商信息，否则会发送失败
        defaultConfig.put("smsSecretId", "default_secret_id");
        defaultConfig.put("smsSecretKey", "default_secret_key");
        defaultConfig.put("smsSdkAppId", "default_app_id");
        defaultConfig.put("smsSign", "默认签名");
        return defaultConfig;
    }

    private String setTemplate() {
        // 使用阿里云短信配置
        String type = "aliyun";
        try {
            String cachedType = RedisUtil.get(SMS_ACTIVE_TYPE_CACHE_KEY);
            if (cachedType != null && !cachedType.trim().isEmpty()) {
                type = cachedType.trim();
                System.out.println("=== 命中短信通道类型缓存: " + type);
            } else {
                ResponseResult<String> resultResp = baseServiceClient.querySmsStates();
                System.out.println("=== querySmsStates 返回结果: " + resultResp);
                // querySmsStates 返回的是 ResponseResult<String>，需要提取 data
                if (resultResp != null && resultResp.getCode() == 1 && resultResp.getData() != null) {
                    Object dataObj = resultResp.getData();
                    System.out.println("=== dataObj 类型: " + (dataObj != null ? dataObj.getClass().getName() : "null"));
                    System.out.println("=== dataObj 值: " + dataObj);

                    String result = null;
                    // 如果 data 是 String，直接使用
                    if (dataObj instanceof String) {
                        result = (String) dataObj;
                    } else {
                        // 如果不是 String，尝试转换为字符串（可能是序列化问题）
                        result = dataObj.toString();
                        // 如果包含 JSON 结构，尝试解析
                        if (result != null && result.trim().startsWith("{")) {
                            try {
                                Map<String, Object> jsonMap = JSON.parseObject(result, Map.class);
                                if (jsonMap != null && jsonMap.containsKey("data")) {
                                    result = jsonMap.get("data").toString();
                                }
                            } catch (Exception e) {
                                System.out.println("=== 解析 JSON 失败: " + e.getMessage());
                            }
                        }
                    }

                    if (result != null && !result.trim().isEmpty()) {
                        type = result.trim();
                        RedisUtil.set(SMS_ACTIVE_TYPE_CACHE_KEY, type, SMS_META_CACHE_SECONDS);
                        System.out.println("=== 最终使用的 type: " + type);
                    }
                } else {
                    System.out.println("=== querySmsStates 返回结果为空或失败: code=" + (resultResp != null ? resultResp.getCode() : "null"));
                }
            }
        } catch (Exception e) {
            System.out.println("获取短信配置类型失败，使用默认配置: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 从Redis获取短信配置（使用RedisUtil统一工具类）
        String sms_config = null;
        try {
            sms_config = RedisUtil.get(HouConfig.SMS_CONFIG + "_" + type);
        } catch (Exception e) {
            System.out.println("从Redis获取短信配置失败: " + e.getMessage());
        }
        Map<String, Object> map = null;
        if (com.houjiang.commons.util.StringUtils.isEmpty(sms_config)) {
            try {
                System.out.println("=== 调用 selectSMSInfoByType，type=" + type);
                ResponseResult resultResp = baseServiceClient.selectSMSInfoByType(type);
                // 检查返回结果是否成功
                if (resultResp != null && resultResp.getCode() == 1 && resultResp.getData() != null) {
                    Object data = resultResp.getData();
                    // 如果 data 是 Map，直接使用；如果是 String，尝试解析为 JSON
                    if (data instanceof Map) {
                        map = (Map<String, Object>) data;
                    } else if (data instanceof String) {
                        map = JSON.parseObject((String) data, Map.class);
                    } else {
                        // 尝试转换为 Map
                        map = JSON.parseObject(JSON.toJSONString(data), Map.class);
                    }
                    // 写回 Redis：此前只读不写，导致每次发码都同步打 base，Nacos 抖动即超时
                    if (map != null && !map.isEmpty()) {
                        try {
                            RedisUtil.set(HouConfig.SMS_CONFIG + "_" + type, JSON.toJSONString(map), SMS_META_CACHE_SECONDS);
                            System.out.println("=== 短信配置已缓存到 Redis，TTL=" + SMS_META_CACHE_SECONDS + "s ===");
                        } catch (Exception cacheEx) {
                            System.out.println("=== 短信配置写 Redis 失败（忽略，本次仍可用内存配置）: " + cacheEx.getMessage());
                        }
                    }
                } else {
                    System.out.println("⚠️ 获取短信配置失败，返回结果为空或失败: " + resultResp);
                    System.out.println("⚠️ 警告：将使用默认配置，但默认配置的 AccessKeyId 无效，短信发送会失败");
                    System.out.println("⚠️ 请在 base 服务中配置有效的短信服务商信息（smsStatus=01）");
                    map = getDefaultSmsConfig();
                }
            } catch (Exception e) {
                System.out.println("⚠️ 获取短信配置失败，使用默认配置: " + e.getMessage());
                System.out.println("⚠️ 警告：默认配置的 AccessKeyId 无效，短信发送会失败");
                System.out.println("⚠️ 请在 base 服务中配置有效的短信服务商信息");
                e.printStackTrace();
                map = getDefaultSmsConfig();
            }
        } else {
            map = JSON.parseObject(sms_config, Map.class);
        }
        if (null != map) {
            //初始化SMS配置信息
            TemplateIdConstant.SECRET_ID = StringUtils.StrToString(map.get("smsSecretId"));
            TemplateIdConstant.SECRET_KEY = StringUtils.StrToString(map.get("smsSecretKey"));
            TemplateIdConstant.SMS_SDK_APP_ID = StringUtils.StrToString(map.get("smsSdkAppId"));
            TemplateIdConstant.SIGN = StringUtils.StrToString(map.get("smsSign"));
            // 如果 region 为空，使用默认值 "cn-hangzhou"
            String region = StringUtils.StrToString(map.get("smsRegion"));
            TemplateIdConstant.REGION = (region == null || region.trim().isEmpty()) ? "cn-hangzhou" : region;
            // 注意：以下模板相关字段已从数据库中删除
            // 短信模板现在统一通过消息模板系统管理（hou_base_message_template表）
            // 通用模板ID从消息模板系统获取
            String templateId = StringUtils.StrToString(map.get("smsGeneral"));
            
            // 如果配置中没有模板ID，从消息模板系统查询
            if (templateId == null || templateId.trim().isEmpty()) {
                try {
                    System.out.println("=== 配置中未找到模板ID，从消息模板系统查询 ===");
                    // 查询用户认证相关的短信模板（bizType为AUTH，且支持SMS渠道）
                    Map<String, Object> templateResult = messageTemplateService.getTemplateList(1, 10, "AUTH", null, "SMS", null);
                    @SuppressWarnings("unchecked")
                    List<com.houjiang.thirdparty.entity.message.MessageTemplate> templates = 
                        (List<com.houjiang.thirdparty.entity.message.MessageTemplate>) templateResult.get("list");
                    
                    if (templates != null && !templates.isEmpty()) {
                        // 查找第一个启用的模板
                        com.houjiang.thirdparty.entity.message.MessageTemplate template = templates.stream()
                            .filter(t -> t.getStatus() != null && t.getStatus() == 1)
                            .filter(t -> t.getSmsTemplateCode() != null && !t.getSmsTemplateCode().trim().isEmpty())
                            .findFirst()
                            .orElse(null);
                        
                        if (template != null && template.getSmsTemplateCode() != null) {
                            templateId = template.getSmsTemplateCode().trim();
                            System.out.println("=== 从消息模板系统获取模板ID: " + templateId);
                            System.out.println("=== 模板信息: templateCode=" + template.getTemplateCode() + ", templateName=" + template.getTemplateName() + ", smsTemplateCode=" + template.getSmsTemplateCode());
                        } else {
                            // 如果没有找到启用的模板，尝试使用第一个模板
                            com.houjiang.thirdparty.entity.message.MessageTemplate firstTemplate = templates.get(0);
                            if (firstTemplate.getSmsTemplateCode() != null && !firstTemplate.getSmsTemplateCode().trim().isEmpty()) {
                                templateId = firstTemplate.getSmsTemplateCode().trim();
                                System.out.println("=== 从消息模板系统获取模板ID（未启用）: " + templateId);
                            } else {
                                System.out.println("=== 警告：消息模板系统中未找到有效的短信模板CODE");
                            }
                        }
                    } else {
                        System.out.println("=== 警告：消息模板系统中未找到AUTH类型的短信模板");
                    }
                } catch (Exception e) {
                    System.out.println("=== 从消息模板系统获取模板ID失败: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("=== 从配置中获取模板ID: " + templateId);
            }
            
            TemplateIdConstant.GENERAL = templateId;
            TemplateIdConstant.REGISTER_TEMPLATE_ID = templateId;
            // 其他模板字段已删除，不再使用
            TemplateIdConstant.SHOPPAYRENT_ID = StringUtils.StrToString(map.get("smsShopPayRent"));
            TemplateIdConstant.RESIDENCYQUALIFICATIONS_ID = StringUtils.StrToString(map.get("smsResidencyQualifications"));
            TemplateIdConstant.DEALERREVIEW_ID = StringUtils.StrToString(map.get("smsDealerReview"));
            
            // 打印配置信息（不打印敏感信息）
            System.out.println("=== 短信配置加载完成 ===");
            System.out.println("短信类型: " + type);
            System.out.println("SECRET_ID: " + (TemplateIdConstant.SECRET_ID != null && !TemplateIdConstant.SECRET_ID.isEmpty() ? "已配置" : "未配置"));
            System.out.println("SECRET_KEY: " + (TemplateIdConstant.SECRET_KEY != null && !TemplateIdConstant.SECRET_KEY.isEmpty() ? "已配置" : "未配置"));
            System.out.println("SMS_SDK_APP_ID: " + TemplateIdConstant.SMS_SDK_APP_ID);
            System.out.println("SIGN: " + TemplateIdConstant.SIGN);
            System.out.println("REGION: " + TemplateIdConstant.REGION);
            System.out.println("GENERAL模板ID: " + TemplateIdConstant.GENERAL);
        } else {
            System.out.println("=== 警告：短信配置Map为空，使用默认配置 ===");
        }
        return type;
    }

    @Override
    public ResponseResult sendDuanxin(Map<String, String> paraMap) {
        String type = setTemplate();
        // 从参数映射中获取用户手机号码
        String userMobile = paraMap.get("userMobile");
        // 从参数映射中获取处理结果文本
        String result = paraMap.get("text");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_R.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                //短信模板处理 商品库存不足模板
                String content = "您的商品{1}库存即将耗尽，请尽快补充调整。";
                // 将模板中的占位符{1}替换为实际的结果
                content = content.replace("{1}", result);
                // 调用腾讯短信工具类发送短信，传入用户手机号和替换后的短信内容
                TencentSmsUtils.send(userMobile, content,interDialingCode);

            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.INVENTORY_TEMPLATE_ID,
                        Collections.singletonList(result)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.INVENTORY_TEMPLATE_ID,
                        Arrays.asList(result)));
            }
        }

        return ResponseResult.fail(BizCodeEnume.SENDING_SUCCEEDED.getCode(), BizCodeEnume.SENDING_SUCCEEDED.getMsg());
    }

    @Override
    public ResponseResult shipments(Map<String, String> paraMap) {
        String type = setTemplate();
        // 从参数映射中获取用户手机号
        String userMobile = paraMap.get("userMobile");
        // 从参数映射中获取店铺名称
        String result1 = paraMap.get("shopName");
        // 从参数映射中获取订单配送代码
        String result2 = paraMap.get("orderShippingCode");
        // 从参数映射中获取运输服务名称
        String result3 = paraMap.get("ShippingExpressname");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_S.getCode(),Arrays.asList(result1, result2, result3));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                //短信模板处理 商家发货成功模板
                String content = "您好，您在{1}店铺购买的商品商家已经发货，物流单号为{2}，物流公司是{3}，请确认收货。";
                // 将模板中的占位符替换为实际的变量值
                content = content.replace("{1}", result1)
                        .replace("{2}", result2)
                        .replace("{3}", result3);
                // 发送短信到用户的移动设备
                TencentSmsUtils.send(userMobile, content,interDialingCode);

            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.SHIPMENTS_SUCCEED_ID,
                        Arrays.asList(result1, result2, result3)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.SHIPMENTS_SUCCEED_ID,
                        Arrays.asList(result1, result2, result3)));
            }
        }
        return ResponseResult.fail(BizCodeEnume.SENDING_SUCCEEDED.getCode(), BizCodeEnume.SENDING_SUCCEEDED.getMsg());
    }

    @Override
    public ResponseResult pushTheDelivery(Map<String, String> paraMap) {
        String type = setTemplate();
        // 从参数映射中获取订单卖家联系方式
        String userMobile = paraMap.get("orderSellerContact");
        // 从参数映射中获取订单接收者名称
        String result1 = paraMap.get("orderReceiverName");
        // 从参数映射中获取时间差值，可能表示处理时间或延迟时间
        String result2 = paraMap.get("timeDiff");
        // 从参数映射中获取订单ID，用于唯一标识订单
        String result3 = paraMap.get("orderId");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_T.getCode(),Arrays.asList(result1, result2, result3));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                //短信模板处理  提醒商家发货模板
                String content = "发货提醒：{1}在您店铺购买的商品已超过{2}小时未发货，订单号为{3}，请尽快安排发货，如超过48小时未发货，平台将对您的商品进行下架处理";
                // 将模板中的占位符替换为实际的变量值
                content = content.replace("{1}", result1)
                        .replace("{2}", result2)
                        .replace("{3}", result3);
                // 发送短信到用户的移动设备
                TencentSmsUtils.send(userMobile, content,interDialingCode);

            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.PUSHTheDELIVERY_ID,
                        Arrays.asList(result1, result2, result3)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.PUSHTheDELIVERY_ID,
                        Arrays.asList(result1, result2, result3)));
            }
        }
        return ResponseResult.fail(BizCodeEnume.SENDING_SUCCEEDED.getCode(), BizCodeEnume.SENDING_SUCCEEDED.getMsg());
    }

    @Override
    public ResponseResult goodsObligation(Map<String, String> paraMap) {
        String type = setTemplate();
        // 从参数映射中获取用户手机号
        String userMobile = paraMap.get("userMobile");
        // 从参数映射中获取店铺名称
        String result1 = paraMap.get("shopName");
        // 从参数映射中获取商品名称
        String result2 = paraMap.get("goodsName");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_U.getCode(),Arrays.asList(result1, result2));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                //短信模板处理  提醒用户付款模板
                String content = "商品待付款提醒：您在{1}店铺购买的{2}商品未支付，请尽快支付。";
                // 将预定义的占位符替换为实际的内容
                content = content.replace("{1}", result1)
                        .replace("{2}", result2);
                // 发送包含替换后内容的短信到用户手机
                TencentSmsUtils.send(userMobile, content,interDialingCode);

            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GOODSOBLIGATION_ID,
                        Arrays.asList(result1, result2)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GOODSOBLIGATION_ID,
                        Arrays.asList(result1, result2)));
            }
        }
        return ResponseResult.fail(BizCodeEnume.SENDING_SUCCEEDED.getCode(), BizCodeEnume.SENDING_SUCCEEDED.getMsg());
    }


    @Override
    public ResponseResult checkOldPhone(Map<String, String> paraMap) {
        // 从参数映射中获取电话验证码
        String paraPhoneCode = paraMap.get("phoneCode");
        // 从参数映射中获取令牌字符串
        String tokenString = paraMap.get("token");
        // 从Redis中获取用户信息，使用令牌作为键
        String userinfoString = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + tokenString);
        // 将用户信息字符串解析为UserInfoDTO对象
        UserInfoDTO dto = JSON.parseObject(userinfoString, UserInfoDTO.class);
        // 从DTO中获取用户信息对象
        UserInfoVO userInfo = dto.getUser();
        // 从用户信息中获取手机号码
        String phone = userInfo.getUserMobile();

        // 从Redis中获取对应的电话验证码
        String phoneCode = RedisUtil.get(phone);

        // 检查电话验证码是否过期，如果过期则返回相应的错误信息
        if (null == phoneCode) {
            return ResponseResult.fail(BizCodeEnume.VERIFICATION_CODE_OVERDUE);
        }

        // 检查输入的验证码与Redis中的验证码是否匹配，如果匹配则返回验证成功的消息
        if (paraPhoneCode.equals(phoneCode)) {
            return ResponseResult.success(BizCodeEnume.SUCCESSFULLY_VERIFIED_THE_OLD_PHONE_NUMBER.getMsg());
        }

        // 如果验证码不匹配，则返回验证失败的消息
        return ResponseResult.fail(BizCodeEnume.FAILED_VERIFIED_THE_OLD_PHONE_NUMBER);


    }


    @Override
    public ResponseResult getCode(String token, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //调用工具类生成的验证码和验证码图片
        //用我们的验证码类，生成验证码类对象
        VerifiCode v = new VerifiCode();
        //获取验证码
        BufferedImage image = v.getImage();
        //将验证码的文本存在session中
        req.getSession().setAttribute("text", v.getText());
        //将验证码图片响应给客户端
        VerifiCode.output(image, resp.getOutputStream());

        RedisUtil.set("verification_code_" + token, v.getText(), RedisUtil.EXRP_FIVE_MIN);
        return ResponseResult.success();
    }

    /**
     * 根据传入的参数map检查手机号并发送验证码。
     *
     * @param paraMap 包含用户手机号和店铺ID的参数映射。
     * @return 返回操作结果，成功或失败。
     */
    @Override
    public ResponseResult sjCheckPhoneAndSend(Map<String, String> paraMap) {
        String type = setTemplate();
        /*
         * 使用系统时间直接判断
         * */
        if (!enable) 
        {
            long currentTime2 = System.currentTimeMillis();
            if (currentTime2 >= endTime) {
                enable = true;
            } else {
                midTime = (endTime - currentTime2) / 1000;
                return ResponseResult.fail(0, "请勿多次重复点击");
            }
        }
        enable = false;
        Date date = new Date();
        currentTime = date.getTime();
        endTime = currentTime + 60000;

        /* 从参数映射中获取用户手机号和店铺ID */
        String userMobile = paraMap.get("userMobile");
        String shopId = paraMap.get("shopId");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";

        /**
         * 校验用户是否已经注册
         */
        /* 根据手机号检查用户是否注册 */
        String userId = userServiceClient.checkPhoneNumber(paraMap);

        /* 如果用户未注册，则返回失败结果 */
        if (null == userId || "".equals(userId)) {
            return ResponseResult.fail(0, "手机号不存在");
        }

        /* 生成6位随机验证码 */
        // 6位验证码
        String result = "";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            result += random.nextInt(10);
        }
        System.out.println("验证码验证码验证码验证码验证码验证码验证码验证码验证码验证码验证码验证码验证码验证码验证码:"+result);
        /* 将验证码存储到Redis中，有效期为5分钟 */
        RedisUtil.set(userMobile, result, 300);
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile))
        {
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            /* 根据配置决定使用哪种短信模板发送验证码 */
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                //短信模板处理 通用模板  修改手机号
                TencentSmsUtils.general(userMobile, result,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Collections.singletonList(result)));
            } else {
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Arrays.asList(result)));
            }
        }

        /* 操作成功，返回成功结果 */
        return ResponseResult.success();
    }

    @Override
    public ResponseResult verifiPhoneCodeselfshop(Map<String, String> paraMap) {
        String type = setTemplate();
        // 从参数映射中获取用户手机号
        String userMobile = paraMap.get("userMobile");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";

        // 初始化验证码结果字符串
        // 6位验证码
        String result = "";
        // 创建随机数生成器用于生成验证码
        Random random = new Random();
        // 循环生成6位数字验证码
        for (int i = 0; i < 6; i++) {
            // 将随机生成的数字追加到验证码字符串中
            result += random.nextInt(10);
        }
        // 将验证码存储到Redis中，键为用户手机号加前缀，值为验证码，过期时间为58秒
        RedisUtil.set(HouConfig.HOU_USER_RESTRICT + userMobile, result, 58);
        // 将验证码直接以用户手机号为键存储到Redis中，过期时间为300秒
        RedisUtil.set(userMobile, result, 300);
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            // 根据ifunimall的值决定使用哪种方式发送短信
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                // 如果ifunimall为真，使用通用模板发送短信，修改手机号
                // 短信模板处理 通用模板  修改手机号
                TencentSmsUtils.general(userMobile, result,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Collections.singletonList(result)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Arrays.asList(result)));
            }
        }
        // 返回成功的响应结果，包含生成的验证码
        return ResponseResult.success(result);

    }

    @Override
    public ResponseResult sendCode(Map<String, String> paraMap) {
        // 从参数映射中获取用户手机号
        String userMobile = paraMap.get("userMobile");
        
        // 参数验证
        if (userMobile == null || userMobile.trim().isEmpty()) {
            return ResponseResult.fail(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "手机号不能为空");
        }
        
        // 获取场景类型（register-注册，login-登录，forgot-忘记密码，其他-通用）
        // 优先 scene；type 仅在是场景名（非 1/2 通道）时回退，避免注册页 type=1 被当成非注册场景
        String scene = paraMap.get("scene");
        if (scene == null || scene.trim().isEmpty()) {
            String typeParam = paraMap.get("type");
            if (typeParam != null && !typeParam.trim().isEmpty()) {
                String trimmedType = typeParam.trim();
                if ("register".equalsIgnoreCase(trimmedType)
                        || "login".equalsIgnoreCase(trimmedType)
                        || "forgot".equalsIgnoreCase(trimmedType)
                        || "reset".equalsIgnoreCase(trimmedType)) {
                    scene = trimmedType;
                }
            }
        }
        if (scene == null || scene.trim().isEmpty()) {
            scene = "login";
        }
        // 调试日志：打印接收到的参数
        System.out.println("=== 发送短信验证码 ===");
        System.out.println("接收到的参数: " + paraMap);
        System.out.println("接收到的 scene 参数: " + paraMap.get("scene"));
        System.out.println("接收到的 type 参数: " + paraMap.get("type"));
        System.out.println("最终使用的场景类型 scene: " + scene);
        System.out.println("手机号 userMobile: " + userMobile);

        // 注册场景优先校验手机号是否已注册（先于频控，避免已注册号被频控文案挡住）
        if ("register".equalsIgnoreCase(scene.trim())) {
            System.out.println("=== 注册场景，检查手机号是否已注册 ===");
            String userId = userServiceClient.checkPhoneNumber(paraMap);
            if (null != userId && !"".equals(userId)) {
                System.out.println("=== 手机号已注册，返回错误 ===");
                return ResponseResult.fail(0, "该手机号已被注册");
            }
        } else {
            System.out.println("=== 非注册场景（" + scene + "），跳过手机号检查 ===");
        }
        
        // ========== 防刷机制优先：避免频控请求也同步拉短信配置/打运营商 ==========
        // 检查同一手机号是否在60秒内发送过验证码
        String smsRateLimitKey = "SMS_RATE_LIMIT_" + userMobile;
        String lastSendTimeStr = RedisUtil.get(smsRateLimitKey);
        if (lastSendTimeStr != null) {
            long lastTime = Long.parseLong(lastSendTimeStr);
            long currentTime = System.currentTimeMillis();
            // 间隔秒数
            long interval = (currentTime - lastTime) / 1000;
            if (interval < 60) {
                long remainingSeconds = 60 - interval;
                System.out.println("=== 防刷检查：手机号 " + userMobile + " 在 " + interval + " 秒前发送过验证码，还需等待 " + remainingSeconds + " 秒 ===");
                return ResponseResult.fail(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), 
                    "发送过于频繁，请" + remainingSeconds + "秒后再试");
            }
        }
        
        // ========== 防刷机制：限制同一IP发送频率（仅做拦截，计数在发送成功后更新） ==========
        // 从参数中获取IP地址（如果Controller传递了IP）
        String clientIp = paraMap.get("clientIp");
        if (clientIp != null && !clientIp.isEmpty()) {
            String ipRateLimitKey = "SMS_IP_RATE_LIMIT_" + clientIp;
            String ipSendCountStr = RedisUtil.get(ipRateLimitKey);
            int count = ipSendCountStr != null ? Integer.parseInt(ipSendCountStr) : 0;
            if (count >= 10) {
                System.out.println("=== 防刷检查：IP " + clientIp + " 在10分钟内已发送 " + count + " 次验证码，超过限制 ===");
                return ResponseResult.fail(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), 
                    "请求过于频繁，请稍后再试");
            }
            // 注意：IP计数在发送成功后才更新，避免提前计数导致误判
        }

        // 频控通过后再加载短信通道配置（可能涉及 base Feign）
        String type = setTemplate();
        
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";
        // 获取消息模板信息（用于记录）
        com.houjiang.thirdparty.entity.message.MessageTemplate messageTemplate = null;
        try {
            messageTemplate = messageTemplateService.getTemplateByCode("USER_AUTH_VERIFY_CODE");
        } catch (Exception e) {
            System.out.println("=== 获取消息模板失败: " + e.getMessage());
        }
        
        // 初始化验证码结果字符串
        // 6位验证码
        String result = "";
        // 创建随机数生成器对象
        Random random = new Random();
        // 生成6位随机数字验证码
        for (int i = 0; i < 6; i++) {
            result += random.nextInt(10);
        }
        
        // 判断是否是邮箱
        boolean sendSuccess = false;
        String failReason = null;
        String channel = CheckPhoneUtil.isValidEmail(userMobile) ? "EMAIL" : "SMS";
        
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(), Arrays.asList(result));
            if(!start){
                failReason = "邮件发送失败";
                // 保存发送记录（失败，异步）
                asyncSaveMessageRecord(messageTemplate, channel, userMobile, "验证码", "您的验证码是：" + result, false, failReason);
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
            sendSuccess = true;
        }else{
            try {
                // 选择发送验证码的短信模板
                if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                    // 短信模板处理 通用模板  修改手机号
                    System.out.println("=== 使用unimall短信通道发送验证码 ===");
                    TencentSmsUtils.general(userMobile, result,interDialingCode);
                    sendSuccess = true;
                } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                    // 系统外发送测试短信
                    System.out.println("=== 使用腾讯云短信通道发送验证码 ===");
                    System.out.println("手机号: " + (interDialingCode + userMobile));
                    // 从模板中获取短信模板ID
                    // 默认值
                    String smsTemplateId = TemplateIdConstant.GENERAL;
                    if (messageTemplate != null && messageTemplate.getSmsTemplateCode() != null && !messageTemplate.getSmsTemplateCode().trim().isEmpty()) {
                        smsTemplateId = messageTemplate.getSmsTemplateCode().trim();
                        System.out.println("=== 从消息模板获取短信模板ID: " + smsTemplateId);
                    } else {
                        System.out.println("=== 警告：未找到消息模板或模板ID为空，使用默认模板ID: " + smsTemplateId);
                    }
                    System.out.println("模板ID: " + smsTemplateId);
                    System.out.println("验证码: " + result);
                    //发送短信
                    TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), smsTemplateId,
                            Collections.singletonList(result)));
                    sendSuccess = true;
                } else {
                    System.out.println("=== 使用阿里云短信通道发送验证码 ===");
                    System.out.println("手机号: " + (interDialingCode + userMobile));
                    // 从模板中获取短信模板ID
                    // 默认值
                    String smsTemplateId = TemplateIdConstant.GENERAL;
                    if (messageTemplate != null && messageTemplate.getSmsTemplateCode() != null && !messageTemplate.getSmsTemplateCode().trim().isEmpty()) {
                        smsTemplateId = messageTemplate.getSmsTemplateCode().trim();
                        System.out.println("=== 从消息模板获取短信模板ID: " + smsTemplateId);
                    } else {
                        System.out.println("=== 警告：未找到消息模板或模板ID为空，使用默认模板ID: " + smsTemplateId);
                    }
                    System.out.println("模板ID: " + smsTemplateId);
                    System.out.println("验证码: " + result);
                    System.out.println("地域: " + TemplateIdConstant.REGION);
                    System.out.println("签名: " + TemplateIdConstant.SIGN);
                    //发送短信
                    AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), smsTemplateId,
                            Arrays.asList(result)));
                    sendSuccess = true;
                }
                System.out.println("=== 短信发送成功，验证码已存储到Redis ===");
            } catch (DefinedException e) {
                // 捕获短信发送异常，记录详细错误信息
                System.out.println("=== 短信发送失败（DefinedException） ===");
                System.out.println("错误码: " + e.getMessageCode());
                System.out.println("错误信息: " + e.getMessage());
                System.out.println("短信类型: " + type);
                System.out.println("手机号: " + userMobile);
                String smsTemplateId = TemplateIdConstant.GENERAL;
                if (messageTemplate != null && messageTemplate.getSmsTemplateCode() != null) {
                    smsTemplateId = messageTemplate.getSmsTemplateCode();
                }
                System.out.println("模板ID: " + smsTemplateId);
                e.printStackTrace();
                failReason = e.getMessageCode() + ": " + e.getMessage();
                // 保存发送记录（失败，异步，不阻塞错误返回）
                asyncSaveMessageRecord(messageTemplate, channel, userMobile, "验证码", "您的验证码是：" + result, false, failReason);
                // 返回具体的错误信息
                return ResponseResult.fail(e.getMessageCode(), e.getMessage());
            } catch (Exception e) {
                // 捕获其他未知异常
                System.out.println("=== 短信发送失败（未知异常） ===");
                System.out.println("异常类型: " + e.getClass().getName());
                System.out.println("异常信息: " + e.getMessage());
                System.out.println("短信类型: " + type);
                System.out.println("手机号: " + userMobile);
                System.out.println("模板ID: " + TemplateIdConstant.GENERAL);
                e.printStackTrace();
                failReason = "短信发送失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误");
                // 保存发送记录（失败，异步，不阻塞错误返回）
                asyncSaveMessageRecord(messageTemplate, channel, userMobile, "验证码", "您的验证码是：" + result, false, failReason);
                // 返回通用错误信息
                return ResponseResult.fail(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), failReason);
            }
        }
        
        // ========== 从配置获取验证码有效期（分钟），默认30分钟；优先本地 Redis 缓存 ==========
        int codeExpireMinutes = resolveCodeExpireMinutes();
        
        // 转换为秒
        int codeExpireSeconds = codeExpireMinutes * 60;
        
        // ========== 存储验证码到Redis，过期时间从配置读取 ==========
        // 统一使用RedisUtil存储验证码（commons封装的统一工具类）
        RedisUtil.set(userMobile, result, codeExpireSeconds);
        
        // ========== 记录发送时间，用于防刷检查 ==========
        RedisUtil.set(smsRateLimitKey, String.valueOf(System.currentTimeMillis()), 60);
        
        // ========== 发送成功后更新IP计数 ==========
        if (clientIp != null && !clientIp.isEmpty()) {
            String ipRateLimitKey = "SMS_IP_RATE_LIMIT_" + clientIp;
            String ipSendCountStr = RedisUtil.get(ipRateLimitKey);
            int count = ipSendCountStr != null ? Integer.parseInt(ipSendCountStr) : 0;
            // 10分钟过期
            RedisUtil.set(ipRateLimitKey, String.valueOf(count + 1), 600);
        }
        
        System.out.println("=== 验证码已存储到Redis，过期时间：" + codeExpireMinutes + "分钟（" + codeExpireSeconds + "秒） ===");
        System.out.println("存储的key: " + userMobile);
        System.out.println("存储的value: " + result);
        System.out.println("过期时间: " + codeExpireSeconds + "秒（" + codeExpireMinutes + "分钟）");
        
        // 验证存储是否成功（立即读取验证）
        String storedCode = RedisUtil.get(userMobile);
        System.out.println("立即验证存储结果: " + storedCode);
        System.out.println("存储验证: " + (storedCode != null && result.equals(storedCode)));
        
        // ========== 保存发送记录到数据库（异步，避免拖慢接口响应） ==========
        if (sendSuccess) {
            asyncSaveMessageRecord(messageTemplate, channel, userMobile, "验证码", "您的验证码是：" + result, true, null);
        }
        
        // 返回发送结果（不返回验证码明文，避免安全问题）
        return ResponseResult.success();

    }

    /**
     * 解析验证码有效期：Redis 缓存 → base 配置 → 默认 30 分钟。
     */
    private int resolveCodeExpireMinutes() {
        int codeExpireMinutes = 30;
        try {
            String cachedExpire = RedisUtil.get(SMS_CODE_EXPIRE_CACHE_KEY);
            if (cachedExpire != null && !cachedExpire.trim().isEmpty()) {
                int cached = Integer.parseInt(cachedExpire.trim());
                if (cached >= 1 && cached <= 1440) {
                    return cached;
                }
            }
        } catch (Exception e) {
            System.out.println("=== 读取验证码有效期缓存失败: " + e.getMessage());
        }
        try {
            ResponseResult settingResp = baseServiceClient.getSmsSetting();
            if (settingResp != null && settingResp.getCode() == 1 && settingResp.getData() != null) {
                Object data = settingResp.getData();
                if (data instanceof Map) {
                    Map<String, Object> settingMap = (Map<String, Object>) data;
                    Object codeExpireObj = settingMap.get("codeExpireMinutes");
                    if (codeExpireObj != null) {
                        if (codeExpireObj instanceof Integer) {
                            codeExpireMinutes = (Integer) codeExpireObj;
                        } else {
                            codeExpireMinutes = Integer.parseInt(codeExpireObj.toString().trim());
                        }
                        if (codeExpireMinutes < 1 || codeExpireMinutes > 1440) {
                            System.out.println("=== 验证码有效期配置值超出范围: " + codeExpireMinutes + "，使用默认值30分钟 ===");
                            codeExpireMinutes = 30;
                        }
                    }
                }
            }
            RedisUtil.set(SMS_CODE_EXPIRE_CACHE_KEY, String.valueOf(codeExpireMinutes), SMS_META_CACHE_SECONDS);
        } catch (Exception e) {
            System.out.println("=== 获取验证码有效期配置失败，使用默认值30分钟: " + e.getMessage());
        }
        return codeExpireMinutes;
    }

    private void asyncSaveMessageRecord(com.houjiang.thirdparty.entity.message.MessageTemplate template,
                                        String channel, String receiverContact, String title,
                                        String content, boolean success, String failReason) {
        CompletableFuture.runAsync(() -> {
            try {
                saveMessageRecord(template, channel, receiverContact, title, content, success, failReason);
            } catch (Exception e) {
                System.out.println("=== 异步保存短信发送记录失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 保存消息发送记录
     * @param template 消息模板（可为null）
     * @param channel 发送渠道（SMS/EMAIL等）
     * @param receiverContact 接收人联系方式（手机号/邮箱）
     * @param title 消息标题
     * @param content 消息内容
     * @param success 是否发送成功
     * @param failReason 失败原因（成功时为null）
     */
    private void saveMessageRecord(com.houjiang.thirdparty.entity.message.MessageTemplate template,
                                  String channel, String receiverContact, String title, 
                                  String content, boolean success, String failReason) {
        com.houjiang.thirdparty.entity.message.MessageRecord record = null;
        try {
            if (messageRecordService == null) {
                System.out.println("=== MessageRecordService未注入，跳过保存发送记录 ===");
                return;
            }
            
            record = new com.houjiang.thirdparty.entity.message.MessageRecord();
            
            // 设置模板信息
            if (template != null) {
                record.setTemplateCode(template.getTemplateCode());
                record.setBizType(template.getBizType());
            } else {
                // 如果没有模板，使用默认值
                record.setTemplateCode("USER_AUTH_VERIFY_CODE");
                record.setBizType("AUTH");
            }
            
            // 设置渠道和接收人信息
            record.setChannel(channel);
            record.setReceiverContact(receiverContact);
            record.setTitle(title);
            record.setContent(content);
            
            // 设置接收人ID（必填字段）
            // 尝试从SecurityContext获取当前用户ID
            String receiverId = null;
            try {
                com.houjiang.commons.vo.user.UserInfoVO currentUser = 
                    com.houjiang.commons.util.SecurityContextUtils.getCurrentUser();
                if (currentUser != null && currentUser.getUserId() != null) {
                    receiverId = currentUser.getUserId().toString();
                }
            } catch (Exception e) {
                // 如果获取用户ID失败，忽略（可能是未登录用户）
                System.out.println("=== 无法获取当前用户ID（可能是未登录用户）: " + e.getMessage());
            }
            
            // 如果无法获取用户ID，使用手机号作为临时ID（对于验证码场景，用户可能未登录）
            if (receiverId == null || receiverId.trim().isEmpty()) {
                // 使用手机号/邮箱作为临时ID，确保字段不为空
                receiverId = receiverContact != null ? receiverContact : "UNKNOWN";
            }
            record.setReceiverId(receiverId);
            
            // 设置接收人姓名（可选）
            try {
                com.houjiang.commons.vo.user.UserInfoVO currentUser = 
                    com.houjiang.commons.util.SecurityContextUtils.getCurrentUser();
                if (currentUser != null && currentUser.getUserName() != null) {
                    record.setReceiverName(currentUser.getUserName());
                }
            } catch (Exception e) {
                // 如果获取用户姓名失败，忽略
            }
            
            // 设置发送状态：0待发送 1发送中 2成功 3失败
            record.setSendStatus(success ? 2 : 3);
            record.setFailReason(failReason);
            
            // 如果发送成功，设置发送时间
            if (success) {
                record.setSendTime((int)(System.currentTimeMillis() / 1000));
            }
            
            record.setRetryCount(0);
            
            // 保存记录
            boolean saved = messageRecordService.createRecord(record);
            if (saved) {
                System.out.println("=== 发送记录已保存到数据库 ===");
                System.out.println("渠道: " + channel);
                System.out.println("接收人: " + receiverContact);
                System.out.println("接收人ID: " + receiverId);
                System.out.println("状态: " + (success ? "成功" : "失败"));
                System.out.println("记录ID: " + record.getId());
            } else {
                System.out.println("=== 保存发送记录失败 ===");
                System.out.println("记录信息: " + com.alibaba.fastjson.JSON.toJSONString(record));
                // 打印更详细的错误信息
                System.out.println("模板编码: " + record.getTemplateCode());
                System.out.println("业务类型: " + record.getBizType());
                System.out.println("渠道: " + record.getChannel());
                System.out.println("接收人ID: " + record.getReceiverId());
                System.out.println("接收人联系方式: " + record.getReceiverContact());
                System.out.println("发送状态: " + record.getSendStatus());
            }
        } catch (Exception e) {
            System.out.println("=== 保存发送记录异常: " + e.getMessage());
            System.out.println("异常类型: " + e.getClass().getName());
            e.printStackTrace();
            // 打印记录信息以便调试
            if (record != null) {
                try {
                    System.out.println("尝试保存的记录信息: " + com.alibaba.fastjson.JSON.toJSONString(record));
                } catch (Exception jsonEx) {
                    System.out.println("无法序列化记录信息: " + jsonEx.getMessage());
                }
            } else {
                System.out.println("记录对象未创建，无法打印详细信息");
            }
        }
    }

    @Override
    public ResponseResult sjverifiphonecode(Map<String, String> paraMap) throws ParseException {
        String type = setTemplate();
        // 从参数映射中获取用户手机号
        String userMobile = paraMap.get("userMobile");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";
        // 根据手机号检查用户是否已注册
        String userId = userServiceClient.checkPhoneNumber(paraMap);
        // 如果用户ID不为空，表示手机号已被注册，返回失败结果
        if (!StringUtils.isEmpty(userId)) {
            return ResponseResult.fail(0, "该手机号已被注册");
        }
        // 从Redis中获取用户手机号的限制信息
        String restrict = RedisUtil.getRedis(HouConfig.HOU_USER_RESTRICT + userMobile);
        // 如果限制信息不为空且不为空字符串，表示该手机号存在限制，返回失败结果
        if (null != restrict && !("".equals(restrict))) {
            return ResponseResult.fail(BizCodeEnume.VALID_SMS_EXCEPTION.getCode(), BizCodeEnume.VALID_SMS_EXCEPTION.getMsg());
        }

        // 从参数映射中获取签名信息
        String sign = paraMap.get("sign");
        // 如果签名信息为空或为空字符串，返回失败结果
        if (null == sign || ("").equals(sign)) {
            return ResponseResult.fail(BizCodeEnume.SIGNATURE_VERIFICATION_FAILED);
        }
        // 格式化当前日期为指定格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
        String signTime = df.format(new Date());
        Date date = df.parse(signTime);
        long signLong = date.getTime();
        // 构造用于生成密码的字符串
        // 该前缀是短信网关约定的签名盐值，与网关侧一致。改动会导致签名校验失败、全站短信不可用。
        String singString = "fkgou" + signLong / 1000 + userMobile.substring(7, 11);
        // 使用MD5算法生成密码
        String password = MessageMD5Util.encoder32ByMd5(singString);
        // 如果生成的密码与参数中的签名不一致，返回失败结果
        if (!password.equals(sign)) {
            return ResponseResult.fail(BizCodeEnume.SIGNATURE_VERIFICATION_FAILED);
        }


        // 6位验证码
        String result = "";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            result += random.nextInt(10);
        }
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                //短信模板处理 通用模板  修改手机号
                TencentSmsUtils.general(userMobile, result,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Collections.singletonList(result)));
            } else {
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Arrays.asList(result)));
            }
        }
        RedisUtil.set(userMobile, result, 300);
        RedisUtil.set(HouConfig.HOU_USER_RESTRICT + userMobile, result, 58);

        return ResponseResult.success();
    }


    @Override
    public ResponseResult checkNewphone(Map<String, String> paraMap) {
        String type = setTemplate();
        // 通过userServiceClient检查手机号是否已被注册
        String userId = userServiceClient.checkPhoneNumber(paraMap);
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";
        // 如果手机号已被注册，则返回失败响应
        if (null != userId) {
            return ResponseResult.fail(0, "该手机号或邮箱已被注册");
        }

        // 从参数映射中获取用户手机号
        String userMobile = paraMap.get("userMobile");
        // 初始化验证码结果字符串
        // 6位验证码
        String result = "";
        // 创建随机数生成器用于生成验证码
        Random random = new Random();
        // 循环生成6位数字验证码
        for (int i = 0; i < 6; i++) {
            result += random.nextInt(10);
        }
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            // 根据ifunimall标志决定使用哪种方式发送短信
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                // 如果是原风模式，使用通用模板发送包含验证码的短信
                // 短信模板处理 通用模板  修改手机号
                TencentSmsUtils.general(userMobile, result,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Collections.singletonList(result)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Arrays.asList(result)));
            }
        }
        // 将验证码存储到Redis中，有效期为5分钟
        RedisUtil.set(userMobile, result, 300);
        // 返回成功响应，包含生成的验证码
        return ResponseResult.success(result);

    }

    /**
     * 根据传入的参数获取签名工具类实例。
     * 该方法主要用于处理注册场景下的用户手机号和签名的验证与存储。
     *
     * @param paraMap 包含用户手机号和签名的参数映射。
     * @return 返回处理结果，如果参数缺失或无效，则返回失败响应，否则返回成功响应并包含加密后的签名。
     */
    @Override
    public ResponseResult getSignUtil(Map<String, String> paraMap) {
        /* 从参数映射中提取签名和用户手机号 */
        /* s 1.生成验证码 2.把验证码保存再redis中 */
        String sign = paraMap.get("parameter");
        String userMobile = paraMap.get("userMobile");

        /* 检查签名和用户手机号是否为空，如果为空则返回参数验证失败的响应 */
        if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(userMobile)) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg());
        }

        /* 使用MD5算法加密签名，并将加密后的密码存储到Redis中，有效期为60秒 */
        String password = MessageMD5Util.encoder32ByMd5(sign);
        // 前端生成的 签存入redis
        RedisUtil.set("REGISTER_USERMOBILE_" + userMobile, password, 60);

        /* 返回成功响应，并包含加密后的签名 */
        return ResponseResult.success(password);
    }

    @Override
    public ResponseResult verifiPhoneCode(Map<String, String> paraMap) {
        String type = setTemplate();
        // 初始化一个用于返回结果的映射
        Map<String, String> returnMap = new HashMap<String, String>();

        // 从参数映射中获取用户手机号和店铺ID
        String userMobile = paraMap.get("userMobile");
        String shopId = paraMap.get("shopId");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";

        // 根据手机号检查用户ID是否存在
        String userId = userServiceClient.checkPhoneNumber(paraMap);

        // 如果用户ID存在，则表示手机号已注册，返回相应的成功信息
        if (StrUtil.isNotEmpty(userId)) {
            if(CheckPhoneUtil.isValidEmail(userMobile)){
                // 已存在时返回错误信息。
                return ResponseResult.fail(BizCodeEnume.EMAIL_EXSIT_EXCEPTION);
            }else{
                // 已存在时返回错误信息。
                return ResponseResult.fail(BizCodeEnume.PHONE_EXSIT_EXCEPTION);
            }
        }

        // 从Redis中获取用户手机号的限制信息，用于防止频繁操作
        String restrict = RedisUtil.getRedis(HouConfig.HOU_USER_RESTRICT + userMobile);

        // 如果存在限制信息，则表示该操作已被限制，返回相应的成功信息
        if (null != restrict && !(restrict.isEmpty())) {
            return ResponseResult.success(BizCodeEnume.REPEATED_SENDING.getCode(), BizCodeEnume.REPEATED_SENDING.getMsg());
        }


//        String sign = paraMap.get("sign");
//        if (null == sign || ("").equals(sign)) {
//            return ResponseResult.success(1,"验签失败",4);
//        }
//        String singString = RedisUtil.get("REGISTER_USERMOBILE_"+userMobile);//从redis中取出签 验证
//        if (null == singString || !singString.equals(sign)) {
//            return ResponseResult.success(1,"验签失败",4);
//        }
//        //验签成功 从reids 中删除签
//        RedisUtil.remove("REGISTER_USERMOBILE_"+userMobile);

        // 6位验证码
        String result = "";
        // 初始化随机数生成器，用于生成验证码
        Random random = new Random();

        // 生成一个6位数的随机验证码
        for (int i = 0; i < 6; i++) {
            result += random.nextInt(10);
        }

        // 将生成的验证码以用户手机号为键存储到Redis中，有效期60秒
        RedisUtil.set(HouConfig.HOU_USER_RESTRICT + userMobile, result, 60);
        // 同时以用户手机号直接作为键存储验证码，有效期5分钟
        RedisUtil.set(userMobile, result, 300);
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            // 根据条件决定发送哪种类型的短信
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                // 短信模板处理 通用模板  修改手机号
                TencentSmsUtils.general(userMobile, result,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Collections.singletonList(result)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Arrays.asList(result)));
            }
        }

        // 返回生成的验证码
        return ResponseResult.success(result);

    }

    @Override
    public ResponseResult verifiPhoneCode2(Map<String, Object> map) {
        String type = setTemplate();
        ResponseResult rr = new ResponseResult();
        String token = (String) map.get("token");
        // 获取电话区号
        String interDialingCode = null;
        Object interDialingCodeObj = map.get("interDialingCode");
        if(null != interDialingCodeObj && StringUtils.isNotBlank(interDialingCodeObj.toString())){
            interDialingCode = interDialingCodeObj.toString();
        }else{
            interDialingCode = "+86";
        }
        try {
            // 接收不做处理
            String userMobile = (String) map.get("userMobile");
            // 检查是否存在验证码会话ID，如果存在，则删除该验证码
            if (RedisUtil.exists(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token)) {
                RedisUtil.remove(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token);
                RedisUtil.remove(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_MOBILE + "_" + token);
            }
            // 从Redis中获取用户令牌对应的用户信息
            String userinfoString = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
            UserInfoDTO dto = JSON.parseObject(userinfoString, UserInfoDTO.class);
            // 获取用户详情
            UserInfoVO userInfo = dto.getUser();
            // 如果用户手机号未设置，从用户信息中获取手机号
            // 提现
            if (userMobile == null) {
                userMobile = userInfo.getUserMobile();
            }

            Random random = new Random();
            // 6位验证码
            String result = "";
            for (int i = 0; i < 6; i++) {
                result += random.nextInt(10);
            }
            // 判断是否是邮箱
            if(CheckPhoneUtil.isValidEmail(userMobile)){
                //邮件模板处理
                Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
                if(!start){
                    return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
                }
            }else{
                // 调用短信模板，参数
                if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                    //短信模板处理 通用模板  修改手机号
                    TencentSmsUtils.general(userMobile, result,interDialingCode);
                } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                    // 系统外发送测试短信
                    //发送短信
                    TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                            Collections.singletonList(result)));
                } else {
                    AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                            Arrays.asList(result)));
                }
            }
            RedisUtil.set(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token, result, RedisUtil.EXRP_HOUR);
            RedisUtil.set(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_MOBILE + "_" + token, userMobile, RedisUtil.EXRP_HOUR);
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), "发送成功", result);
        } catch (Exception e) {
            rr.setCode(500);
            rr.setMessage("verifiPhoneCodeErrorResults:" + e.getMessage());
        }
        return rr;
    }

    /**
     * 发送验证手机号的验证码
     *
     * @param map
     * @return
     */
    @Override
    public ResponseResult checkMobileVerifyCode(Map<String, Object> map) {
        String type = setTemplate();
        ResponseResult rr = new ResponseResult();
        String token = (String) map.get("token");
        // 获取电话区号
        String interDialingCode = null;
        Object interDialingCodeObj = map.get("interDialingCode");
        if(null != interDialingCodeObj && StringUtils.isNotBlank(interDialingCodeObj.toString())){
            interDialingCode = interDialingCodeObj.toString();
        }else{
            interDialingCode = "+86";
        }
        try {
            // 接收不做处理
            String userMobile = (String) map.get("userMobile");
            // 检查是否存在验证码会话ID，如果存在，则删除该验证码
            if (RedisUtil.exists(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token)) {
                RedisUtil.remove(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token);
                RedisUtil.remove(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_MOBILE + "_" + token);
            }
            // 从Redis中获取用户令牌对应的用户信息
            String userinfoString = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + token);
            UserInfoDTO dto = JSON.parseObject(userinfoString, UserInfoDTO.class);
            // 获取用户详情
            UserInfoVO userInfo = dto.getUser();
            // 如果用户手机号未设置，从用户信息中获取手机号
            // 提现
            if (userMobile == null) {
                userMobile = userInfo.getUserMobile();
            }
            // 初始化返回参数映射，并放入用户手机号
            Map<String, String> returnMap = new HashMap<>();
            returnMap.put("userMobile", userMobile);
            // 调用用户服务查询用户账号信息
            ResponseResult resultData = userServiceClient.getUserAccountByMap(returnMap);
            // 如果查询结果失败，则表示手机号不存在，返回对应的错误信息
            if (CommonStatusEnum.FAIL.getCode() == resultData.getCode()) {
                return ResponseResult.fail(BizCodeEnume.MOBILE_NUMBER_NOT_EXIST);
            }
            Random random = new Random();
            // 6位验证码
            String result = "";
            for (int i = 0; i < 6; i++) {
                result += random.nextInt(10);
            }
            // 判断是否是邮箱
            if(CheckPhoneUtil.isValidEmail(userMobile)){
                //邮件模板处理
                Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
                if(!start){
                    return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
                }
            }else{
                // 调用短信模板，参数
                if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                    //短信模板处理 通用模板  修改手机号
                    TencentSmsUtils.general(userMobile, result,interDialingCode);
                } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                    // 系统外发送测试短信
                    //发送短信
                    TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                            Collections.singletonList(result)));
                } else {
                    AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                            Arrays.asList(result)));
                }
            }

            RedisUtil.set(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token, result, RedisUtil.EXRP_HOUR);
            RedisUtil.set(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_MOBILE + "_" + token, userMobile, RedisUtil.EXRP_HOUR);
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), "发送成功", result);
        } catch (Exception e) {
            rr.setCode(500);
            rr.setMessage("verifiPhoneCodeErrorResults:" + e.getMessage());
        }
        return rr;
    }

    @Override
    public ResponseResult plusmemberopenSend(Map<String, String> map) {
        String type = setTemplate();
        // 从map中获取用户手机号和权益信息
        String userMobile = map.get("userMobile");
        String result1 = map.get("result1");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(map.get("interDialingCode"))?map.get("interDialingCode"):"+86";
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_V.getCode(),Arrays.asList(result1));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            // 判断是否是unimall平台
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                // 构造短信内容，将权益信息插入模板中
                String content = "尊敬的用户，您好！您已获得PLUS{1}权益，将于24小时内到达账户中心，您可登录网站查看";
                content = content.replace("{1}", result1);
                // 发送短信
                TencentSmsUtils.send(userMobile, content,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.PLUSMEMBER_ID,
                        Collections.singletonList(result1)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.PLUSMEMBER_ID, Arrays.asList(result1)));
            }
        }

        // 返回发送成功的响应结果
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), "发送成功");
    }

    @Override
    public ResponseResult sendPhoneCodeByToken(Map<String, String> paraMap) {
        String type = setTemplate();
        // 从参数映射中获取token字符串
        String tokenString = paraMap.get("token");
        // 从Redis中获取用户信息字符串，使用token作为键
        String userinfoString = RedisUtil.get(HouConfig.HOU_USER_TOKEN_USER + tokenString);
        // 将用户信息字符串解析为UserInfoDTO对象
        UserInfoDTO dto = JSON.parseObject(userinfoString, UserInfoDTO.class);
        // 从DTO中获取UserInfoVO对象
        UserInfoVO userInfo = dto.getUser();
        // 从用户信息中获取用户手机号
        String userMobile = userInfo.getUserMobile();
        // 获取电话区号
        String interDialingCode = userInfo.getInternationalDialingCode();
        interDialingCode = StringUtils.isNotBlank(interDialingCode)?interDialingCode:"+86";
        // 初始化验证码字符串
        // 6位验证码
        String result = "";
        // 创建随机数生成器
        Random random = new Random();
        // 生成6位随机数字验证码
        for (int i = 0; i < 6; i++) {
            result += random.nextInt(10);
        }
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            // 短信模板处理 通用模板
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                TencentSmsUtils.general(userMobile, result,interDialingCode);
            } else if (type.equals(CommonType.smsType.ALIYUN.getDesc())) {
                // 阿里云发送
                AliyunSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Collections.singletonList(result)));
            } else {
                TencentSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL, Arrays.asList(result)));
            }
        }

        // 将验证码存储到Redis中，以用户手机号为键
        RedisUtil.set(userMobile, result, RedisUtil.EXRP_FIVE_MIN);
        // 返回成功的响应结果，包含生成的验证码
        return ResponseResult.success(result);

    }

    @Override
    public ResponseResult threeCheckPhoneAndSend(Map<String, String> paraMap) {
        String type = setTemplate();
        // 从参数映射中获取用户手机号
        String userMobile = paraMap.get("userMobile");
        // 根据手机号通过用户服务客户端检查用户ID
        String userId = userServiceClient.checkPhoneNumber(paraMap);
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";

        // 如果用户ID为空，则表示手机号不存在，返回相应的错误响应
        if (null == userId) {
            return ResponseResult.fail(BizCodeEnume.MOBILE_NUMBER_NOT_EXIST.getCode(), BizCodeEnume.MOBILE_NUMBER_NOT_EXIST.getMsg());
        }
        // 生成6位随机验证码
        // 6位验证码
        String result = RandomUtil.randomNumbers(6);
        // 从Redis中获取当前手机号对应的验证码
        String sessionPhoneCode = RedisUtil.getNoExpire(userMobile);
        if (!StringUtils.isEmpty(sessionPhoneCode)) {
            return ResponseResult.fail(BizCodeEnume.REPEATED_SENDING.getCode(), BizCodeEnume.REPEATED_SENDING.getMsg());
        }
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            // 选择发送短信的模板
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                // 短信模板处理，使用通用模板
                //短信模板处理 通用模板
                TencentSmsUtils.general(userMobile, result,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Collections.singletonList(result)));
            } else {
                // 发送测试短信
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Arrays.asList(result)));
                // 初始化用于存储对象名称的列表
                ArrayList<String> objectName = new ArrayList<String>();
                // 将验证码添加到对象名称列表中
                objectName.add(result);
            }
        }
        // 返回生成的验证码
        RedisUtil.set(userMobile, result, RedisUtil.EXRP_ONE_MIN);
        return ResponseResult.success(result);

    }

    @Override
    public ResponseResult userPlusRenew(Map<String, String> paraMap) {
        String type = setTemplate();
        // 从参数映射中获取用户手机号
        String userMobile = paraMap.get("userMobile");

        // 从参数映射中获取活动结果
        String result = paraMap.get("result");
        // 获取电话区号
        String interDialingCode = StringUtils.isNotBlank(paraMap.get("interDialingCode"))?paraMap.get("interDialingCode"):"+86";
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_V.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                // 构造短信内容，将权益名称插入到模板中
                String content = "尊敬的用户，您好！您已获得PLUS{1}权益，将于24小时内到达账户中心，您可登录网站查看";
                content = content.replace("{1}", result);
                // 发送短信给用户
                TencentSmsUtils.send(userMobile, content,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.PLUSMEMBER_ID,
                        Collections.singletonList(result)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.PLUSMEMBER_ID,
                        Arrays.asList(result)));
            }
        }

        // 返回操作成功的响应结果
        return ResponseResult.success();

    }

    @Override
    public ResponseResult checkVerificationCode(Map<String, Object> map) {
        // 从请求参数中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中是否存在，以确认用户会话的有效性
        boolean isExist = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExist) {
            // 从请求参数中获取用户输入的验证码
            // 获取验证码
            String verifyCode = (String) map.get("verifyCode");
            // 从Redis中获取对应的验证码
            String code = RedisUtil.get(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token);
            // 比较用户输入的验证码和Redis中的验证码是否一致
            if (verifyCode.equals(code)) {
                // 验证码正确，返回成功响应
                return ResponseResult.success(1, "验证码正确！");
            }
            // 验证码错误，返回失败响应
            return ResponseResult.fail(2, "验证码错误！");
        }
        // 若token不存在，说明会话已过期或无效，删除对应的验证码，防止安全隐患
        //短信校验后就删除
        RedisUtil.remove(HouConfig.HOU_VERIFICODE_CLIENT_SESSION_ID + "_" + token);
        // 返回token无效的错误响应
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);

    }

    @Override
    public ResponseResult sendRetentionMoneyMessage(String userMobile, String result1,String interDialingCode) {
        String type = setTemplate();
        // 获取电话区号
        interDialingCode = StringUtils.isNotBlank(interDialingCode) ? interDialingCode : "+86";
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_W.getCode(),Arrays.asList(result1));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            // 根据ifunimall的值决定是否发送正式短信。
            // 如果ifunimall为true，则发送包含质保金补缴金额的短信通知。
            // 如果ifunimall为false，则再次发送测试短信。
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                // 构造包含质保金补缴金额的短信内容。
                String content = "亲爱的卖家您好，根据平台质保金规则您需补缴{1}元质保金，请登录商家后台查询。";
                content = content.replace("{1}", result1);
                TencentSmsUtils.send(userMobile, content,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.RETENTIONMONEY_ID,
                        Collections.singletonList(result1)));
            } else {
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.RETENTIONMONEY_ID,
                        Arrays.asList(result1)));
            }
        }

        // 返回操作成功的响应结果。
        return ResponseResult.success();

    }

    @Override
    public ResponseResult storeQualification(Map map) {
        String type = setTemplate();
        // 将map中用户手机号对应的值转换为字符串类型，避免null值导致的异常
        String userMobile = String.valueOf(map.get("userMobile"));
        // 将map中result1对应的值转换为字符串类型，用于后续处理或展示
        String result1 = String.valueOf(map.get("result1"));
        // 将map中result2对应的值转换为字符串类型，用于后续处理或展示
        String result2 = String.valueOf(map.get("result2"));
        // 将map中result3对应的值转换为字符串类型，用于后续处理或展示
        String result3 = String.valueOf(map.get("result3"));

        //短信通知类型   1 企业资质审核短信，2店铺缴纳租金短信，3店铺入驻资质审核短信，4物流通知短信
        Integer smstype = (Integer) map.get("smstype");
        // 获取电话区号
        String interDialingCode = null;
        Object interDialingCodeObj = map.get("interDialingCode");
        if(null != interDialingCodeObj && StringUtils.isNotBlank(interDialingCodeObj.toString())){
            interDialingCode = interDialingCodeObj.toString();
        }else{
            interDialingCode = "+86";
        }

        //短信通知
        if (smstype == 1) {
            // 判断是否是邮箱
            if(CheckPhoneUtil.isValidEmail(userMobile)){
                //邮件模板处理
                Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Z.getCode(),Arrays.asList(result1, result2));
                if(!start){
                    return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
                }
            }else{
                if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                    //短信模板处理 商家发货成功模板
                    String content = "亲爱的{1}您好，您提交的企业信息审核结果为：{2}";
                    content = content.replace("{1}", result1)
                            .replace("{2}", result2);
                    TencentSmsUtils.send(userMobile, content,interDialingCode);
                } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                    // 系统外发送测试短信
                    //发送短信
                    TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.ENTERPRISEAUDIT_ID,
                            Arrays.asList(result1, result2)));
                } else {
                    //发送短信
                    AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.ENTERPRISEAUDIT_ID,
                            Arrays.asList(result1, result2)));
                }
            }

        } else if (smstype == 2) {
            // 判断是否是邮箱
            if(CheckPhoneUtil.isValidEmail(userMobile)){
                //邮件模板处理
                Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Y.getCode(),Arrays.asList(result1, result2));
                if(!start){
                    return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
                }
            }else{
                if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                    //短信模板处理 商家发货成功模板
                    String content = "亲爱的商家您好，{1}您提交的租金付款凭证{2}";
                    content = content.replace("{1}", result1)
                            .replace("{2}", result2);
                    TencentSmsUtils.send(userMobile, content,interDialingCode);
                } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                    // 系统外发送测试短信
                    //发送短信
                    TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.SHOPPAYRENT_ID,
                            Arrays.asList(result1, result2)));
                } else {
                    //发送短信
                    AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.SHOPPAYRENT_ID,
                            Arrays.asList(result1, result2)));
                }
            }

        } else if (smstype == 3) {
            // 判断是否是邮箱
            if(CheckPhoneUtil.isValidEmail(userMobile)){
                //邮件模板处理
                Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_X.getCode(),Arrays.asList(result1, result2));
                if(!start){
                    return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
                }
            }else{
                if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                    //短信模板处理 商家发货成功模板
                    String content = "亲爱的商家您好，您入驻的店铺{1}开店资质审核，{2}";
                    content = content.replace("{1}", result1)
                            .replace("{2}", result2);
                    TencentSmsUtils.send(userMobile, content,interDialingCode);
                } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                    // 系统外发送测试短信
                    //发送短信
                    TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.RESIDENCYQUALIFICATIONS_ID,
                            Arrays.asList(result1, result2)));
                } else {
                    //发送短信
                    AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.RESIDENCYQUALIFICATIONS_ID,
                            Arrays.asList(result1, result2)));
                }
            }

        } else if (smstype == 4) {
            // 判断是否是邮箱
            if(CheckPhoneUtil.isValidEmail(userMobile)){
                //邮件模板处理
                Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_S.getCode(),Arrays.asList(result1, result2, result3));
                if(!start){
                    return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
                }
            }else{
                if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                    //短信模板处理 商家发货成功模板
                    String content = "您好，您在{1}店铺购买的商品商家已经发货，物流单号为{2}，物流公司是{3}，请确认收货。";
                    content = content.replace("{1}", result1)
                            .replace("{2}", result2)
                            .replace("{3}", result3);
                    TencentSmsUtils.send(userMobile, content,interDialingCode);
                } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                    // 系统外发送测试短信
                    //发送短信
                    TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.SHIPMENTS_SUCCEED_ID,
                            Arrays.asList(result1, result2, result3)));
                } else {
                    //发送短信
                    AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.SHIPMENTS_SUCCEED_ID,
                            Arrays.asList(result1, result2, result3)));
                }
            }
        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult dealerReview(Map map) {
        String type = setTemplate();
        // 将用户手机号码从Map中提取并转换为字符串形式
        String userMobile = String.valueOf(map.get("userMobile"));
        // 将第一个结果从Map中提取并转换为字符串形式
        String result1 = String.valueOf(map.get("result1"));
        // 将第二个结果从Map中提取并转换为字符串形式
        String result2 = String.valueOf(map.get("result2"));
        // 获取电话区号
        String interDialingCode = null;
        Object interDialingCodeObj = map.get("interDialingCode");
        if(null != interDialingCodeObj && StringUtils.isNotBlank(interDialingCodeObj.toString())){
            interDialingCode = interDialingCodeObj.toString();
        }else{
            interDialingCode = "+86";
        }
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_A_1.getCode(),Arrays.asList(result1, result2));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                //短信模板处理 商家发货成功模板
                String content = "亲爱的商家您好，您申请成为{1}店铺的经销商审核被驳回，驳回原因: {2}";
                // 将预定义的占位符替换为实际的内容
                content = content.replace("{1}", result1)
                        .replace("{2}", result2);
                // 发送包含替换后内容的短信到用户手机
                TencentSmsUtils.send(userMobile, content,interDialingCode);

            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.DEALERREVIEW_ID,
                        Arrays.asList(result1, result2)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.DEALERREVIEW_ID,
                        Arrays.asList(result1, result2)));
            }
        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult sendMessageRider(String userMobile,String interDialingCode) {
        String type = setTemplate();
        // 获取电话区号
        interDialingCode = StringUtils.isNotBlank(interDialingCode) ? interDialingCode:"+86";
        // 验证用户输入的手机号码是否为有效的中国手机号码
        boolean isValid = isValidChinesePhoneNumber(userMobile);
        if (!isValid) {
            // 如果手机号码无效，返回错误响应，提示用户重新输入
            return ResponseResult.fail(0, "手机号码有误请重新输入");
        }

        // 通过手机号码查询骑手信息，检查该手机号是否已被注册。
        // 降级口径（方案 §1.4.4）：骑手配送是可选商业插件，插件未安装时查不到服务，
        // 按「非骑手」放行，不得让 Feign 调用失败冒泡成 500 把主流程打断。
        String riderId;
        try {
            riderId = riderServiceClient.queryRiderInfoByMobile(userMobile);
        } catch (Exception e) {
            log.warn("查询骑手信息失败，按「非骑手」放行: mobile={} reason={}", userMobile, e.getMessage());
            riderId = null;
        }
        if (null != riderId) {
            // 如果查询到骑手信息，表示手机号已被注册，返回错误响应
            return ResponseResult.fail(0, "该手机号已被注册");
        }

        // 初始化验证码字符串
        // 6位验证码
        String result = "";
        // 创建随机数生成器，用于生成验证码
        Random random = new Random();
        // 循环6次，每次生成一个0-9的随机数，并追加到验证码字符串
        for (int i = 0; i < 6; i++) {
            result += random.nextInt(10);
        }

        // 将验证码存储到Redis中，分别以用户手机号和特定前缀的键存储，设置不同的过期时间
        RedisUtil.set(HouConfig.HOU_USER_RESTRICT + userMobile, result, 60);
        RedisUtil.set(userMobile, result, 300);
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            // 根据配置决定是否发送验证码到腾讯云短信服务
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                // 调用相应的方法发送验证码
                TencentSmsUtils.general(userMobile, result,interDialingCode);
            } else if (type.equals(CommonType.smsType.TENCENT_CLOUD.getDesc())) {
                // 系统外发送测试短信
                //发送短信
                TencentSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Collections.singletonList(result)));
            } else {
                //发送短信
                AliyunSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL, Arrays.asList(result)));
            }
        }

        // 返回生成的验证码
        return ResponseResult.success(result);

    }

    @Override
    public ResponseResult riderWithdrawalCode(String userMobile,String interDialingCode) {
        // 获取电话区号
        interDialingCode = StringUtils.isNotBlank(interDialingCode) ? interDialingCode:"+86";
        String type = setTemplate();
        // 初始化为空字符串，用于存储最终生成的6位验证码
        // 6位验证码
        String result = "";
        // 创建随机数对象，用于生成验证码
        Random random = new Random();
        // 循环6次，每次生成一个0-9的随机数，并追加到result字符串中
        for (int i = 0; i < 6; i++) {
            result += random.nextInt(10);
        }
        // 将生成的验证码存储到Redis中，键为用户手机号加上前缀，值为验证码，过期时间为60秒
        RedisUtil.set(HouConfig.HOU_USER_RESTRICT + userMobile, result, 60);
        // 将生成的验证码存储到Redis中，键为用户手机号，值为验证码，过期时间为300秒
        RedisUtil.set(userMobile, result, 300);
        // 判断是否是邮箱
        if(CheckPhoneUtil.isValidEmail(userMobile)){
            //邮件模板处理
            Boolean start = MailUtil.sendMail(userMobile, CommonType.messageTemplate.CODE_Q.getCode(),Arrays.asList(result));
            if(!start){
                return ResponseResult.fail(BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getCode(), BizCodeEnume.MOBILE_EMAIL_IS_ERROR.getMsg());
            }
        }else{
            // 根据ifunimall的值，决定是否调用正式的短信发送接口还是测试接口
            if (type.equals(CommonType.smsType.UNIMALL.getDesc())) {
                TencentSmsUtils.general(userMobile, result,interDialingCode);
            } else if (type.equals(CommonType.smsType.ALIYUN.getDesc())) {
                // 阿里云发送
                AliyunSmsUtils.testSms(new TencentSmsDTO(Collections.singletonList(interDialingCode + userMobile), TemplateIdConstant.GENERAL,
                        Collections.singletonList(result)));
            } else {
                TencentSmsUtils.testSms(new TencentSmsDTO(Arrays.asList(interDialingCode + userMobile), TemplateIdConstant.GENERAL, Arrays.asList(result)));
            }
        }
        // 返回生成的验证码
        return ResponseResult.success(result);

    }

    public static boolean isValidChinesePhoneNumber(String phoneNumber) {
        // 定义手机号的正则表达式
        String regex = "^1[3-9]\\d{9}$";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        // 匹配手机号
        Matcher matcher = pattern.matcher(phoneNumber);
        // 返回匹配结果
        return matcher.matches();
    }

    @Override
    public ResponseResult sendSms(Map<String, Object> paraMap) {
        String type = setTemplate();
        // 调用短信模板，参数
        paraMap.put("type", type);
        // 从参数中提取模板代码，优先使用messageTemplateId，如果没有则使用templateCode
        Object templateCodeObj = paraMap.get("messageTemplateId");
        if (templateCodeObj == null) {
            templateCodeObj = paraMap.get("templateCode");
        }
        if (templateCodeObj == null) {
            return ResponseResult.fail("messageTemplateId或templateCode参数不能为空");
        }
        String str = templateCodeObj.toString();
        // 根据模板代码选择相应的策略，并执行
        SmsStrategy strategy = strategies.get(str);
        if (strategy == null) {
            return ResponseResult.fail("未找到对应的短信策略，messageTemplateId: " + str + "，可用的策略: " + strategies.keySet());
        }
        strategy.execute(paraMap);
        return ResponseResult.success();
    }

    @Override
    public ResponseResult queryDeliveryDetails(Map<String, String> paraMap) {
        String userMobile = paraMap == null ? null : paraMap.get("userMobile");
        if (userMobile == null || userMobile.trim().isEmpty()) {
            return ResponseResult.fail(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(), "手机号不能为空");
        }
        // 去除区号前缀，阿里云回执查询只接受纯手机号
        String phone = userMobile.trim();
        if (phone.startsWith("+86")) {
            phone = phone.substring(3);
        } else if (phone.startsWith("86") && phone.length() == 13) {
            phone = phone.substring(2);
        }
        // 发送日期：默认当天，阿里云回执仅支持近30天查询
        String sendDate = paraMap.get("sendDate");
        if (sendDate == null || sendDate.trim().isEmpty()) {
            sendDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        }
        // 加载当前短信通道配置（回执查询仅阿里云通道支持）
        String type = setTemplate();
        if (!CommonType.smsType.ALIYUN.getDesc().equalsIgnoreCase(type)) {
            return ResponseResult.fail(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(),
                    "当前短信通道(" + type + ")不支持回执查询，仅阿里云通道支持");
        }
        java.util.List<java.util.Map<String, Object>> details = AliyunSmsUtils.queryDeliveryDetails(phone, sendDate.trim());
        if (details.isEmpty()) {
            return ResponseResult.fail(GlobalExceptionEnum.SMS_EXCEPTION.getErrorCode(),
                    "未查到该手机号" + sendDate + "的短信发送记录（回执仅保留30天）");
        }
        // 取最近一条回执的友好文案作为提示信息，便于前端直接展示
        java.util.Map<String, Object> latest = details.get(0);
        java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("latestStatus", latest.get("sendStatusText"));
        data.put("latestErrMsg", latest.get("errMsg"));
        data.put("details", details);
        return ResponseResult.success(data);
    }
}
