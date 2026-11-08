package com.houjiang.thirdparty.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.thirdparty.service.SendSmsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SendSmsService sendSmsService;

    /**
     * 平台端发送库存不足短信
     *
     * @param paraMap
     */
    @PostMapping("/register/duanxin")
    public ResponseResult sendDuanxin(@RequestBody Map<String, String> paraMap) {
        return sendSmsService.sendDuanxin(paraMap);
    }

    /**
     * 商家端发货成功提醒短信
     *
     * @param paraMap
     */
    @PostMapping("/register/shipments")
    public ResponseResult shipments(@RequestBody Map<String, String> paraMap) {
        return sendSmsService.shipments(paraMap);
    }

    /**
     * 平台发送提醒发货短信
     *
     * @param paraMap
     */
    @PostMapping("/register/pushTheDelivery")
    public ResponseResult pushTheDelivery(@RequestBody Map<String, String> paraMap) {
        return sendSmsService.pushTheDelivery(paraMap);
    }

    /**
     * 商家提醒用户付款
     *
     * @param paraMap
     */
    @PostMapping("/register/goodsObligation")
    public ResponseResult goodsObligation(@RequestBody Map<String, String> paraMap) {
        return sendSmsService.goodsObligation(paraMap);
    }

    /**
     * 验证旧手机验证码是否正确
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/checkoldphone")
    public ResponseResult checkOldPhone(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = sendSmsService.checkOldPhone(paraMap);
        return result;

    }

    /**
     * 生成图形验证码
     *
     * @param token
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @GetMapping("/getCode")
    public ResponseResult getCode(@RequestParam("token") String token, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseResult result = sendSmsService.getCode(token, req, resp);
        return result;
    }

    /**
     * 发送注册验证码
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/register/sjcheckphoneandsend")
    public ResponseResult sjCheckPhoneAndSend(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = sendSmsService.sjCheckPhoneAndSend(paraMap);
        return result;
    }

    /**
     * 发送手机验证码
     *
     * @param paraMap 发送验证码参数 (userMobile, interDialingCode)
     * @return ResponseResult
     */
    @PostMapping("/register/selfshopphonecode")
    public ResponseResult verifiPhoneCodeselfshop(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = sendSmsService.verifiPhoneCodeselfshop(paraMap);
        return result;
    }

    /**
     * 开通plus会员短信
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/userPlus/renew")
    public ResponseResult userPlusRenew(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = sendSmsService.userPlusRenew(paraMap);
        return result;
    }

    /**
     * 注册公司账号--获取验证码
     *
     * @param paraMap
     * @return
     * @throws IOException
     */
    @PostMapping("/sendCode")
    public ResponseResult sendCode(@RequestBody Map<String, String> paraMap, HttpServletRequest request) {
        // 最外层兜底：无论 Service 内部抛什么异常（Redis 不可用/远程调用失败/NPE 等），
        // 都必须返回规范的 ResponseResult，避免 HTTP 500 冒泡到 BFF 导致前端只看到
        // "短信服务暂时不可用" 的统一文案，无法区分真实根因。
        try {
            if (paraMap == null) {
                return ResponseResult.fail(BizCodeEnume.SMS_EXCEPTION.getCode(), "请求参数不能为空");
            }
            // 获取客户端IP地址并传递给Service层用于防刷检查
            String clientIp = getClientIpAddress(request);
            paraMap.put("clientIp", clientIp);
            return sendSmsService.sendCode(paraMap);
        } catch (Exception e) {
            // 暴露真实根因，便于 BFF 侧 Kibana 日志定位
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            log.error("/sms/sendCode 执行异常 userMobile={}, scene={}, errType={}, rootType={}, rootMsg={}",
                    paraMap == null ? null : paraMap.get("userMobile"),
                    paraMap == null ? null : paraMap.get("type"),
                    e.getClass().getName(), root.getClass().getName(), root.getMessage(), e);
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),
                    "短信发送失败：" + root.getClass().getSimpleName()
                            + (root.getMessage() != null ? " - " + root.getMessage() : ""));
        }
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        // 处理IPv6本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    /**
     * 移动端注册短信
     */
    @PostMapping("/register/sjverifiphonecode")
    public ResponseResult sjverifiphonecode(@RequestBody Map<String, String> paraMap) throws ParseException {
        ResponseResult result = sendSmsService.sjverifiphonecode(paraMap);
        return result;
    }

    /**
     * 获取新手机的验证码,并验证新手机是否已经绑定
     */
    @PostMapping("/checkNewphone")
    public ResponseResult checkNewphone(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = sendSmsService.checkNewphone(paraMap);
        return result;
    }

    /**
     * 是否是正确手机号
     */
    @PostMapping("register/getSignUtil")
    public ResponseResult getSignUtil(@RequestBody Map<String, String> paraMap) throws IOException {
        ResponseResult result = sendSmsService.getSignUtil(paraMap);
        return result;
    }

    /**
     * pc端注册短信
     */
    @PostMapping(value = "/register/verifiphonecode")
    public ResponseResult verifiPhoneCode(@RequestBody Map<String, String> paraMap) {
        return sendSmsService.verifiPhoneCode(paraMap);
    }


    /**
     * 发送手机验证码
     *
     * @param map
     * @return
     */
    @PostMapping(value = "/verifiPhoneCode2")
    public ResponseResult verifiPhoneCode2(@RequestBody Map<String, Object> map) {
        return sendSmsService.verifiPhoneCode2(map);
    }

    /**
     * 发送验证手机号的验证码
     *
     * @param map
     * @return
     */
    @PostMapping("/checkMobileVerifyCode")
    public ResponseResult checkMobileVerifyCode(@RequestBody Map<String, Object> map) {
        return sendSmsService.checkMobileVerifyCode(map);
    }

    /**
     * plus 会员 充值短信
     */
    @PostMapping(value = "/plusmemberopenSend")
    public ResponseResult plusmemberopenSend(@RequestBody Map<String, String> map) {
        return sendSmsService.plusmemberopenSend(map);
    }

    /**
     * 根据token获取验证码
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/sendPhoneCodeByToken")
    public ResponseResult sendPhoneCodeByToken(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = sendSmsService.sendPhoneCodeByToken(paraMap);
        return result;
    }

    /**
     * 发送手机验证码
     *
     * @param paraMap
     * @return
     * @throws IOException
     */
    @PostMapping("/code/threecheckphoneandsend")
    public ResponseResult threeCheckPhoneAndSend(@RequestBody Map<String, String> paraMap) throws IOException {
        return sendSmsService.threeCheckPhoneAndSend(paraMap);
    }

    /**
     * 验证修改支付密码的验证码是否正确
     *
     * @param map
     * @return
     */
    @PostMapping("/checkVerificationCode")
    public ResponseResult checkVerificationCode(@RequestBody Map<String, Object> map) {
        return sendSmsService.checkVerificationCode(map);
    }

    /**
     * 发送缴纳质保金短信
     *
     * @param userMobile
     * @param result1
     */
    @GetMapping("/sendRetentionMoneyMessage")
    public ResponseResult sendRetentionMoneyMessage(@RequestParam("userMobile") String userMobile, @RequestParam("result1") String result1,@RequestParam(value ="interDialingCode", required = false) String interDialingCode) {
        //短信通知
        return sendSmsService.sendRetentionMoneyMessage(userMobile, result1,interDialingCode);
    }

    /**
     * 发送店铺资质短信
     */
    @RequestMapping("/storeQualification")
    public ResponseResult storeQualification(@RequestBody Map map) {
        return sendSmsService.storeQualification(map);
    }

    /**
     * 发送经销商审核短信
     */
    @RequestMapping("/dealerReview")
    public ResponseResult dealerReview(@RequestBody Map map) {
        return sendSmsService.dealerReview(map);
    }

    /**
     * 发送短信-骑手
     *
     * @param userMobile
     * @return
     */
    @PostMapping("/register/sendMessageRider")
    public ResponseResult sendMessageRider(@RequestParam("userMobile") String userMobile,@RequestParam(value ="interDialingCode", required = false) String interDialingCode) {
        return sendSmsService.sendMessageRider(userMobile,interDialingCode);
    }

    /**
     * 骑手提现校验手机号
     *
     * @param userMobile
     * @return
     */
    @PostMapping("/rider/withdrawalCode")
    public ResponseResult riderWithdrawalCode(@RequestParam("userMobile") String userMobile,@RequestParam(value ="interDialingCode", required = false)String interDialingCode) {
        return sendSmsService.riderWithdrawalCode(userMobile,interDialingCode);
    }

    /**
     * 使用POST方法通过HTTP协议发送短信
     * 该方法委托给SendSmsService进行具体的短信发送操作
     *
     * @param paraMap 包含所有请求参数的映射对象，具体参数说明由SendSmsService定义
     * @return 返回一个ResponseResult对象，其中包含操作结果
     * <p>
     * 为什么使用PostMapping：需要通过POST方式提交数据，因为可能涉及敏感信息且数据量可能较大
     * 为什么是sendSms：明确该方法的功能是发送短信
     * 为什么是Map类型：为了灵活接收各种可能的短信发送参数，不局限于固定的几个字段
     */
    @PostMapping("/sendSms")
    public ResponseResult sendSms(@RequestBody Map<String, Object> paraMap) {
        return sendSmsService.sendSms(paraMap);
    }

    /**
     * 查询手机号当天的短信发送回执（运营商回执）。
     * 用于排查“发送接口返回成功但收不到短信”：同步受理成功不代表投递成功，
     * 如签名未完成运营商实名制报备（PORT_NOT_REGISTERED）会被运营商拒收。
     * 返回结果不含短信内容，避免验证码泄露。
     *
     * @param paraMap 查询参数：userMobile-必填；sendDate-选填（yyyyMMdd，默认当天）
     * @return ResponseResult，data 含 latestStatus/latestErrMsg/details 回执信息
     */
    @PostMapping("/deliveryDetails")
    public ResponseResult deliveryDetails(@RequestBody Map<String, String> paraMap) {
        try {
            return sendSmsService.queryDeliveryDetails(paraMap);
        } catch (Exception e) {
            log.error("/sms/deliveryDetails 执行异常 userMobile={}", paraMap == null ? null : paraMap.get("userMobile"), e);
            return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(), "查询短信回执失败：" + e.getMessage());
        }
    }
}
