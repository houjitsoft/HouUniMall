package com.houjiang.order.controller;

import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import com.houjiang.order.service.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName BaseController
 * @Description 基础controller
 * @Author Amos
 * @Date 2023/5/18 17:39
 * @Version 1.0
 * @author Amos
 */

public class BaseController {

    /** 注入订单用户购物车服务，用于处理用户购物车相关的业务逻辑。 */
    @Autowired
    protected OrderUserCartService orderUserCartService;

    /** 注入订单商品评价服务，用于处理订单商品评价相关的业务逻辑。 */
    @Autowired
    protected OrderGoodsEvaluationService orderGoodsEvaluationService;

    /** 注入订单创建服务，用于处理订单创建相关的业务逻辑。 */
    @Autowired
    protected OrderCreateService orderCreateService;

    /** 注入订单基础服务，用于处理订单基础信息相关的业务逻辑。 */
    @Autowired
    protected OrderBaseService orderBaseService;

    /** 注入订单发票信息服务，用于处理订单发票信息相关的业务逻辑。 */
    @Autowired
    protected OrderInvoiceInfoService orderInvoiceInfoService;

    /** 注入订单商品服务，用于处理订单中商品相关的业务逻辑。 */
    @Autowired
    protected OrderGoodsService orderGoodsService;

    /** 注入订单退换货服务，用于处理订单退换货相关的业务逻辑。 */
    @Autowired
    protected OrderReturnService orderReturnService;

    /** 注入订单退换货原因服务，用于处理订单退换货原因相关的业务逻辑。 */
    @Autowired
    protected OrderReturnReasonYiyatongService orderReturnReasonService;

    /** 注入订单取消原因服务，用于处理订单取消原因相关的业务逻辑。 */
    @Autowired
    protected OrderCancelReasonService orderCancelReasonService;

    /** 注入订单支付凭证服务，用于处理订单支付凭证相关的业务逻辑。 */
    @Autowired
    protected OrderPaymentVocherService orderPaymentVocherService;

    /** 注入订单发票模板信息服务，用于处理订单发票模板信息相关的业务逻辑。 */
    @Autowired
    protected OrderInvoiceTemplateInfoService orderInvoiceTemplateInfoService;

    /** 注入订单发票地址服务，用于处理订单发票地址相关的业务逻辑。 */
    @Autowired
    protected OrderInvoiceAddressService orderInvoiceAddressService;

    /** 注入订单平台退换货服务，用于处理平台发起的订单退换货相关的业务逻辑。 */
    @Autowired
    protected OrderReturnPlatformService orderReturnPlatformService;

    /** 注入订单结算服务，用于处理订单结算相关的业务逻辑。 */
    @Autowired
    protected OrderSettlementService orderSettlementService;

    /** 注入供应商订单创建服务，用于处理供应商创建订单相关的业务逻辑。 */
    @Autowired
    protected OrderSupplierCreateServer orderSupplierCreateServer;

    /** 注入订单佣金服务，用于处理订单佣金相关的业务逻辑。 */
    @Autowired
    protected OrderCommissionService orderCommissionService;

    /** 注入订单投诉服务，用于处理订单投诉相关的业务逻辑。 */
    @Autowired
    protected OrderComplaintsService orderComplaintsService;

    /** 注入订单骑手基础服务，用于处理订单与骑手相关的基本业务逻辑。 */
    @Autowired
    protected OrderRiderBaseService orderRiderBaseService;

    /** 注入余额充值基础服务，用于处理余额充值相关的基本业务逻辑。 */
    @Autowired
    protected OrderBalanceRechargeService orderBalanceRechargeService;

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
