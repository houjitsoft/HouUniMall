package com.houjiang.payment.controller.api;

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
import com.houjiang.payment.service.api.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员管理接口
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/member")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    
    /**
     * 2. 企业会员实名开户
     * 用于企业会员注册
     * 
     * @param request 企业会员实名开户请求
     * @return 企业会员实名开户响应
     */
    @PostMapping("/regenterprise")
    public RegEnterpriseResponse regEnterprise(@RequestBody RegEnterpriseRequest request) {
        log.info("收到企业会员实名开户请求, bizUserId:{}", request.getBizUserId());
        return memberService.regEnterprise(request);
    }
    
    /**
     * 9. 个人会员实名及绑卡
     * 用于分账方个人会员实名注册并绑卡
     * 
     * @param request 个人会员实名及绑卡请求
     * @return 个人会员实名及绑卡响应
     */
    @PostMapping("/regperson")
    public RegPersonResponse regPerson(@RequestBody RegPersonRequest request) {
        log.info("收到个人会员实名及绑卡请求, bizUserId:{}", request.getBizUserId());
        return memberService.regPerson(request);
    }
    
    /**
     * 3. 企业会员开户结果通知
     * 接收企业会员开户的审核结果通知
     * 
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    @PostMapping("/regenterprise/notify")
    public String regEnterpriseNotify(HttpServletRequest httpRequest) {
        log.info("收到企业会员开户结果通知");
        return memberService.handleRegEnterpriseNotify(httpRequest);
    }
    
    /**
     * 4. 会员资料补录
     * 用于企业会员注册影印件补录
     * 
     * @param request 会员资料补录请求
     * @return 会员资料补录响应
     */
    @PostMapping("/regsupplement")
    public RegSupplementResponse regSupplement(@RequestBody RegSupplementRequest request) {
        log.info("收到会员资料补录请求, bizUserId:{}", request.getBizUserId());
        return memberService.regSupplement(request);
    }
    
    /**
     * 5. 会员资料补录结果通知
     * 接收会员资料补录的OCR识别结果通知
     * 
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    @PostMapping("/regsupplement/notify")
    public String regSupplementNotify(HttpServletRequest httpRequest) {
        log.info("收到会员资料补录结果通知");
        return memberService.handleRegSupplementNotify(httpRequest);
    }
    
    /**
     * 6. 会员绑定手机号申请
     * 用于绑定法人手机号或企业授权手机号
     * 
     * @param request 会员绑定手机号申请请求
     * @return 会员绑定手机号申请响应
     */
    @PostMapping("/bindphoneapple")
    public BindPhoneAppleResponse bindPhoneApple(@RequestBody BindPhoneAppleRequest request) {
        log.info("收到会员绑定手机号申请请求, bizUserId:{}, phone:{}", request.getBizUserId(), request.getPhone());
        return memberService.bindPhoneApple(request);
    }
    
    /**
     * 7. 会员绑定手机号确认
     * 会员绑定手机号上送短信验证码
     * 
     * @param request 会员绑定手机号确认请求
     * @return 会员绑定手机号确认响应
     */
    @PostMapping("/bindphoneconfirm")
    public BindPhoneConfirmResponse bindPhoneConfirm(@RequestBody BindPhoneConfirmRequest request) {
        log.info("收到会员绑定手机号确认请求, bizUserId:{}, phone:{}", request.getBizUserId(), request.getPhone());
        return memberService.bindPhoneConfirm(request);
    }
    
    /**
     * 8. 会员绑定手机号结果通知
     * 接收会员绑定手机号结果通知
     * 
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    @PostMapping("/bindphone/notify")
    public String bindPhoneNotify(HttpServletRequest httpRequest) {
        log.info("收到会员绑定手机号结果通知");
        return memberService.handleBindPhoneNotify(httpRequest);
    }
    
    /**
     * 11. 会员协议签约申请
     * 对分账协议或抽佣协议发起签约申请
     *
     * @param request 会员协议签约申请请求
     * @return 会员协议签约申请响应
     */
    @PostMapping("/applymemprotocol")
    public ApplyMemProtocolResponse applyMemProtocol(@RequestBody ApplyMemProtocolRequest request) {
        log.info("收到会员协议签约申请请求, bizUserId:{}, protocolType:{}", request.getBizUserId(), request.getProtocolType());
        return memberService.applyMemProtocol(request);
    }
    
    /**
     * 12. 会员协议签约结果通知
     * 接收会员协议签约结果和审核结果通知
     *
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    @PostMapping("/applymemprotocol/notify")
    public String applyMemProtocolNotify(HttpServletRequest httpRequest) {
        log.info("收到会员协议签约结果通知");
        return memberService.handleApplyMemProtocolNotify(httpRequest);
    }
    
    /**
     * 13. 会员注册邀请码生成
     * 用于生成会员注册信息采集H5页面链接
     *
     * @param request 会员注册邀请码生成请求
     * @return 会员注册邀请码生成响应
     */
    @PostMapping("/generatereginvitelink")
    public GenerateRegInviteLinkResponse generateRegInviteLink(@RequestBody GenerateRegInviteLinkRequest request) {
        log.info("收到会员注册邀请码生成请求, bizUserId:{}, memCategory:{}, chanNo:{}",
                request.getBizUserId(), request.getMemCategory(), request.getChanNo());
        return memberService.generateRegInviteLink(request);
    }
    
    /**
     * 14. 会员注册成功通知
     * 接收会员注册成功结果通知
     *
     * @param httpRequest HTTP请求对象
     * @return success表示处理成功,其他表示失败
     */
    @PostMapping("/memberregsuccess/notify")
    public String memberRegSuccessNotify(HttpServletRequest httpRequest) {
        log.info("收到会员注册成功通知");
        return memberService.handleMemberRegSuccessNotify(httpRequest);
    }
    
    /**
     * 15. 会员绑定收银宝商户号开户
     * 通过绑定收银宝商户号注册会员
     *
     * @param request 会员绑定收银宝商户号开户请求
     * @return 会员绑定收银宝商户号开户响应
     */
    @PostMapping("/regbindsybcusid")
    public RegBindSybCusIdResponse regBindSybCusId(@RequestBody RegBindSybCusIdRequest request) {
        log.info("收到会员绑定收银宝商户号开户请求, bizUserId:{}, sybCusId:{}, chanNo:{}",
                request.getBizUserId(), request.getSybCusId(), request.getChanNo());
        return memberService.regBindSybCusId(request);
    }
}