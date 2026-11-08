package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.member.BindPhoneAppleRequest;
import com.houjiang.payment.dto.member.BindPhoneAppleResponse;
import com.houjiang.payment.dto.member.BindPhoneConfirmRequest;
import com.houjiang.payment.dto.member.BindPhoneConfirmResponse;
import com.houjiang.payment.dto.member.RegEnterpriseRequest;
import com.houjiang.payment.dto.member.RegEnterpriseResponse;
import com.houjiang.payment.dto.member.RegPersonRequest;
import com.houjiang.payment.dto.member.RegPersonResponse;
import com.houjiang.payment.dto.member.RegSupplementRequest;
import com.houjiang.payment.dto.member.RegSupplementResponse;
import com.houjiang.payment.dto.member.ApplyMemProtocolRequest;
import com.houjiang.payment.dto.member.ApplyMemProtocolResponse;
import com.houjiang.payment.dto.member.GenerateRegInviteLinkRequest;
import com.houjiang.payment.dto.member.GenerateRegInviteLinkResponse;
import com.houjiang.payment.dto.member.RegBindSybCusIdRequest;
import com.houjiang.payment.dto.member.RegBindSybCusIdResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 会员服务接口
 * 
 * @author Amos
 */
public interface MemberService {
    
    /**
     * 2. 企业会员实名开户
     * 用于企业会员注册
     * 
     * @param request 企业会员实名开户请求
     * @return 企业会员实名开户响应
     */
    RegEnterpriseResponse regEnterprise(RegEnterpriseRequest request);
    
    /**
     * 9. 个人会员实名及绑卡
     * 用于分账方个人会员实名注册并绑卡
     * 
     * @param request 个人会员实名及绑卡请求
     * @return 个人会员实名及绑卡响应
     */
    RegPersonResponse regPerson(RegPersonRequest request);
    
    /**
     * 3. 企业会员开户结果通知
     * 接收企业会员开户的审核结果通知
     * 
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    String handleRegEnterpriseNotify(HttpServletRequest httpRequest);
    
    /**
     * 4. 会员资料补录
     * 用于企业会员注册影印件补录
     * 
     * @param request 会员资料补录请求
     * @return 会员资料补录响应
     */
    RegSupplementResponse regSupplement(RegSupplementRequest request);
    
    /**
     * 5. 会员资料补录结果通知
     * 接收会员资料补录的OCR识别结果通知
     * 
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    String handleRegSupplementNotify(HttpServletRequest httpRequest);
    
    /**
     * 6. 会员绑定手机号申请
     * 用于绑定法人手机号或企业授权手机号
     * 
     * @param request 会员绑定手机号申请请求
     * @return 会员绑定手机号申请响应
     */
    BindPhoneAppleResponse bindPhoneApple(BindPhoneAppleRequest request);
    
    /**
     * 7. 会员绑定手机号确认
     * 会员绑定手机号上送短信验证码
     * 
     * @param request 会员绑定手机号确认请求
     * @return 会员绑定手机号确认响应
     */
    BindPhoneConfirmResponse bindPhoneConfirm(BindPhoneConfirmRequest request);
    
    /**
     * 8. 会员绑定手机号结果通知
     * 接收会员绑定手机号结果通知
     * 
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    String handleBindPhoneNotify(HttpServletRequest httpRequest);
    
    /**
     * 11. 会员协议签约申请
     * 对分账协议或抽佣协议发起签约申请
     *
     * @param request 会员协议签约申请请求
     * @return 会员协议签约申请响应
     */
    ApplyMemProtocolResponse applyMemProtocol(ApplyMemProtocolRequest request);
    
    /**
     * 12. 会员协议签约结果通知
     * 接收会员协议签约结果和审核结果通知
     *
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    String handleApplyMemProtocolNotify(HttpServletRequest httpRequest);
    
    /**
     * 13. 会员注册邀请码生成
     * 用于生成会员注册信息采集H5页面链接
     *
     * @param request 会员注册邀请码生成请求
     * @return 会员注册邀请码生成响应
     */
    GenerateRegInviteLinkResponse generateRegInviteLink(GenerateRegInviteLinkRequest request);
    
    /**
     * 14. 会员注册成功通知
     * 接收会员注册成功结果通知
     *
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    String handleMemberRegSuccessNotify(HttpServletRequest httpRequest);
    
    /**
     * 15. 会员绑定收银宝商户号开户
     * 通过绑定收银宝商户号注册会员
     *
     * @param request 会员绑定收银宝商户号开户请求
     * @return 会员绑定收银宝商户号开户响应
     */
    RegBindSybCusIdResponse regBindSybCusId(RegBindSybCusIdRequest request);
}
