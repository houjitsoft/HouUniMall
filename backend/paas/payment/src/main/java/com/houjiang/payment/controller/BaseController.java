package com.houjiang.payment.controller;

/**
 * @author Amos
 * @date 2023/5/18 15:03
 */

import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import com.houjiang.payment.service.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础Controller
 */
public class BaseController {

    @Autowired
    protected PayUserBaseService payUserBaseService;

    @Autowired
    protected PayConsumeRecordService payConsumeRecordService;

    @Autowired
    protected PayConsumeTradeService payConsumeTradeService;

    @Autowired
    protected PayUserResourceService payUserResourceService;

    @Autowired
    protected PayCardInfoService payCardInfoService;

    @Autowired
    protected PayAssetDetailsService payAssetDetailsService;

    @Autowired
    protected PayEntranceService payEntranceService;

    @Autowired
    protected WeChatPayServices weChatPayServices;

    @Autowired
    protected PayConsumeWithdrawService payConsumeWithdrawService;

    @Autowired
    protected  PayAuthorizationAppletInformationService payAuthorizationAppletInformationService;

    @Autowired
    protected PayCardRecordService payCardRecordService;

    @Autowired
    protected PayCardBaseService payCardBaseService;

    @Autowired
    protected PayCardService payCardService;

    @Autowired
    protected PayConfigShopService payConfigShopService;

    @Autowired
    protected PayFxAccountService payFxAccountService;

    @Autowired
    protected PayFxRecordService payFxRecordService;

    /**
     * 从请求头中获取token
     *
     * @return
     */
    public String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                return authorization.substring(7);
            }
        }
        return null;
    }

    /**
     * 验证token并获取用户信息
     *
     * @return
     */
    public UserInfoVO validateTokenAndGetUser() {
        String token = getTokenFromRequest();
        if (token == null) {
            throw new RuntimeException("Token不能为空");
        }
        return ParseTokenUtils.getUserInfoByToken(token);
    }
}
