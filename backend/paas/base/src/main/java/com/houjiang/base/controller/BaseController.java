package com.houjiang.base.controller;

import com.houjiang.base.service.*;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @ClassName BaseController
 * @Description 基础controller
 * @Author Amos
 * @Date 2023/5/18 17:39
 * @Version 1.0
 * @author Amos
 */

public class BaseController {

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

    @Autowired
    protected BaseTagClassService baseTagClassService;

    @Autowired
    protected BaseMembershipGradeService baseMembershipGradeService;

    @Autowired
    protected BaseIntegralRulesService baseIntegralRulesService;

    @Autowired
    protected BaseSysConfigsService baseSysConfigsService;

    @Autowired
    protected BaseMerchantPlusRuleService baseMerchantPlusRuleService;

    @Autowired
    protected BaseSearchWordService baseSearchWordService;

    @Autowired
    protected BusinessSwitchService businessSwitchService;

    @Autowired
    protected SmsInfoService selectSMSInfo;

    @Autowired
    protected  WebIndexService webIndexService;

    @Autowired
    protected BaseVkSetService baseVkSetService;

    @Autowired
    protected BaseArticleGroupService baseArticleGroupService;

    @Autowired
    protected BaseArticleBaseService baseArticleBaseService;

    @Autowired
    protected BaseDistrictService baseDistrictService;

    @Autowired
    protected BaseExpressService baseExpressService;

    @Autowired
    protected BaseKdniaoConfigService baseKdniaoConfigService;

    @Autowired
    protected BaseMerchantAgreementService baseMerchantAgreementService;

    @Autowired
    protected BaseOssConfigService baseOssConfigService;

    @Autowired
    protected BasePlatformBrankService basePlatformBrankService;

    @Autowired
    protected BaseInvoiceTemplateInfoService baseInvoiceTemplateInfoService;

    @Autowired
    protected BaseMerchantPlusThaliService baseMerchantPlusThaliService;

    @Autowired
    protected BaseModularizationFlieService baseModularizationFlieService;

    @Autowired
    protected BaseModularizationdService baseModularizationdService;

    @Autowired
    protected BaseFloorAdpageService baseFloorAdpageService;

    @Autowired
    protected BaseSmsInfoService baseSmsInfoService;

    @Autowired
    protected BasePcIndexConfigService basePcIndexConfigService;

    @Autowired
    protected BaseWxLiveGoodsService baseWxLiveGoodsService;

    @Autowired
    protected BaseWxLiveHostService baseWxLiveHostService;

    @Autowired
    protected BaseWxLiveStudioService baseWxLiveStudioService;

    @Autowired
    protected BaseSystemMessageService baseSystemMessageService;

    @Autowired
    protected BaseSystemPlatformNavService baseSystemPlatformNavService;

    @Autowired
    protected BaseShopModularizationdService baseShopModularizationdService;

    @Autowired
    protected BaseShareUrlService baseShareUrlService;

    @Autowired
    protected BaseModularizationdCatService baseModularizationdCatService;

    @Autowired
    protected BaseSensitiveService baseSensitiveService;

    @Autowired
    protected BaseSensitiveConfigService baseSensitiveConfigService;

    @Autowired
    protected BaseSensitiveClassificationService baseSensitiveClassificationService;

    @Autowired
    protected BaseSensitiveRecordService baseSensitiveRecordService;

    @Autowired
    protected BaseWebConfigService baseWebConfigService;

    @Autowired
    protected CacheSettingsService cacheSettingsService;
}
