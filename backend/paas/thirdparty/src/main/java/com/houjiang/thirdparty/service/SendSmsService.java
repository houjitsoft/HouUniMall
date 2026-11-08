package com.houjiang.thirdparty.service;

import com.houjiang.commons.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * @author Amos
 */
public interface SendSmsService {

    /**
     * 平台端发送库存不足短信
     *
     * @param paraMap
     * @return
     */
    ResponseResult sendDuanxin(Map<String, String> paraMap);

    /**
     * 商家端发货成功提醒短信
     *
     * @param paraMap
     * @return
     */
    ResponseResult shipments(Map<String, String> paraMap);

    /**
     * 平台发送提醒发货短信
     *
     * @param paraMap
     * @return
     */
    ResponseResult pushTheDelivery(Map<String, String> paraMap);

    /**
     * 商家提醒用户付款
     *
     * @param paraMap
     * @return
     */
    ResponseResult goodsObligation(Map<String, String> paraMap);


    /**
     * 验证旧手机验证码是否正确
     *
     * @param paraMap
     * @return
     */
    ResponseResult checkOldPhone(Map<String, String> paraMap);

    /**
     * 生成图形验证码
     *
     * @param token
     * @param req
     * @param resp
     * @return
     * @throws IOException
     */
    ResponseResult getCode(String token, HttpServletRequest req, HttpServletResponse resp) throws IOException;

    /**
     * 发送注册验证码
     *
     * @param paraMap
     * @return
     */
    ResponseResult sjCheckPhoneAndSend(Map<String, String> paraMap);

    /**
     * 发送注册验证码
     *
     * @param paraMap
     * @return
     */
    ResponseResult verifiPhoneCodeselfshop(Map<String, String> paraMap);

    /**
     * 注册公司账号--获取验证码
     *
     * @param paraMap
     * @return
     */
    ResponseResult sendCode(Map<String, String> paraMap);

    /**
     * 移动端注册短信
     *
     * @param paraMap
     * @return
     */
    ResponseResult sjverifiphonecode(Map<String, String> paraMap) throws ParseException;

    /**
     * 获取新手机的验证码,并验证新手机是否已经绑定
     *
     * @param paraMap
     * @return
     */
    ResponseResult checkNewphone(Map<String, String> paraMap);

    /**
     * 是否是正确手机号
     *
     * @param paraMap
     * @return
     */
    ResponseResult getSignUtil(Map<String, String> paraMap);

    /**
     * pc端注册短信
     *
     * @param paraMap
     * @return
     */
    ResponseResult verifiPhoneCode(Map<String, String> paraMap);


    /**
     * 发送手机验证码
     *
     * @param map
     * @return
     */
    ResponseResult verifiPhoneCode2(Map<String, Object> map);

    /**
     * 发送验证手机号的验证码
     *
     * @param map
     * @return
     */
    ResponseResult checkMobileVerifyCode(Map<String, Object> map);

    /**
     * plus 会员 充值短信
     *
     * @param map
     * @return
     */
    ResponseResult plusmemberopenSend(Map<String, String> map);

    /**
     * 根据token获取验证码
     *
     * @param paraMap
     * @return
     */
    ResponseResult sendPhoneCodeByToken(Map<String, String> paraMap);


    /**
     * 发送手机验证码
     *
     * @param paraMap
     * @return
     */
    ResponseResult threeCheckPhoneAndSend(Map<String, String> paraMap);

    /**
     * 开通plus会员短信
     *
     * @param paraMap
     * @return
     */
    ResponseResult userPlusRenew(Map<String, String> paraMap);

    /**
     * 验证修改支付密码的验证码是否正确
     *
     * @param map
     * @return
     */
    ResponseResult checkVerificationCode(Map<String, Object> map);

    /**
     * 发送缴纳质保金短信
     *
     * @param userMobile
     * @param result1
     * @param interDialingCode
     */
    ResponseResult sendRetentionMoneyMessage(String userMobile, String result1,String interDialingCode);

    /**
     * 发送店铺资质短信
     *
     * @param map
     * @return
     */
    ResponseResult storeQualification(Map map);

    /**
     * 发送经销商审核短信
     */
    ResponseResult dealerReview(Map map);

    /**
     * 发送短信-骑手
     *
     * @param userMobile
     * @param interDialingCode
     */
    ResponseResult sendMessageRider(String userMobile,String interDialingCode);

    /**
     * 骑手提现校验手机号
     *
     * @param userMobile
     * @return
     */
    ResponseResult riderWithdrawalCode(String userMobile,String interDialingCode);

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
    ResponseResult sendSms(Map<String, Object> paraMap);

    /**
     * 查询手机号当天的短信发送回执（阿里云运营商回执）。
     * 用于排查“发送接口返回成功但收不到短信”的问题：同步受理成功不代表投递成功，
     * 真实结果以运营商回执为准（如签名未报备会被运营商拒收）。
     *
     * @param paraMap 查询参数：userMobile-必填；sendDate-选填，格式 yyyyMMdd，默认当天
     * @return ResponseResult，data 为回执明细列表（不含短信内容，避免验证码泄露）
     */
    ResponseResult queryDeliveryDetails(Map<String, String> paraMap);
}
