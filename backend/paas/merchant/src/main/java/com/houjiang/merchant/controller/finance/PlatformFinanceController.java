package com.houjiang.merchant.controller.finance;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallRefund;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.exception.BaseException;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.service.finance.IFinanceRechargerService;
import com.houjiang.merchant.service.finance.IFinanceWalletService;
import com.houjiang.merchant.service.mall.IMallRefundService;
import com.houjiang.merchant.service.mall.IMallShopAccountLogService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.finance.FinanceRechargeSearchValidate;
import com.houjiang.merchant.validate.finance.FinanceWalletSearchValidate;
import com.houjiang.merchant.validate.mall.MallRefundReRefundValidate;
import com.houjiang.merchant.validate.mall.MallRefundSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopAccountLogSearchValidate;
import com.houjiang.merchant.vo.finance.FinanceRechargeListVo;
import com.houjiang.merchant.vo.finance.FinanceWalletListVo;
import com.houjiang.merchant.vo.mall.MallRefundListedVo;
import com.houjiang.merchant.vo.mall.MallRefundSumVo;
import com.houjiang.merchant.vo.mall.MallShopAccountLogListedVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商城平台专用财务入口。
 *
 * 该入口只接受 mall 服务使用 Cloud/商城管理员 JWT 转发的请求，不建立商家
 * ThreadLocal，因此查询范围是整个平台，而不是某一家店铺。
 */
@RestController
@RequestMapping("/merchant/platform-finance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MALL_PLATFORM_FINANCE')")
public class PlatformFinanceController {

    private final IMallRefundService refundService;
    private final IMallShopAccountLogService shopAccountLogService;
    private final IFinanceWalletService walletService;
    private final IFinanceRechargerService rechargerService;
    private final MallShopMapper shopMapper;

    @ExceptionHandler(BaseException.class)
    public static ResponseResult<Object> handleBaseException(BaseException exception) {
        return ResponseResult.fail(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public static ResponseResult<Object> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseResult.fail(400, exception.getMessage());
    }

    @PostMapping("/center")
    public ResponseResult<Map<String, Object>> center() {
        BigDecimal shopBalance = shopMapper.selectList(null).stream()
                .filter(shop -> shop.getDeleteTime() == null || shop.getDeleteTime() == 0L)
                .map(shop -> shop.getMoney() == null ? BigDecimal.ZERO : shop.getMoney())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        MallRefundSumVo refund = refundService.sums(null);
        return ResponseResult.success(Map.of(
                "shopBalance", shopBalance,
                "refundMoney", amount(refund.getSuccess()),
                "refundWaitMoney", amount(refund.getWait())
        ));
    }

    @PostMapping("/refund/stats")
    public ResponseResult<MallRefundSumVo> refundStats(@RequestBody(required = false) Map<String, Object> ignored) {
        return ResponseResult.success(refundService.sums(null));
    }

    @PostMapping("/refund/list")
    public ResponseResult<PageResult<MallRefundListedVo>> refundList(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(refundService.list(page(params), refundSearch(params)));
    }

    @PostMapping("/refund/retry")
    public ResponseResult<Object> retryRefund(@Valid @RequestBody MallRefundReRefundValidate validate) {
        MallRefund refund = requireRefund(validate.getId());
        validate.setShopId(refund.getShopId());
        refundService.reRefund(validate);
        return ResponseResult.success();
    }

    @PostMapping("/shop/balance/list")
    public ResponseResult<PageResult<MallShopAccountLogListedVo>> shopBalanceList(@RequestBody Map<String, Object> params) {
        MallShopAccountLogSearchValidate search = new MallShopAccountLogSearchValidate();
        search.setShopId(integer(params, "shopId", "shop_id"));
        String shopName = string(params, "shop_name", "shopName");
        if (search.getShopId() == null && shopName != null) {
            List<Integer> shopIds = shopMapper.selectList(new QueryWrapper<MallShop>()
                            .select("id")
                            .like("name", shopName)
                            .eq("delete_time", 0))
                    .stream().map(MallShop::getId).toList();
            search.setShopIds(shopIds);
        }
        search.setChange_type(integer(params, "change_type", "changeType"));
        search.setIs_gold(integer(params, "is_gold", "isGold"));
        search.setStart_time(string(params, "start_time", "startTime"));
        search.setEnd_time(string(params, "end_time", "endTime"));
        return ResponseResult.success(shopAccountLogService.list(page(params), search));
    }

    @PostMapping("/user/wallet/list")
    public ResponseResult<PageResult<FinanceWalletListVo>> userWalletList(@RequestBody Map<String, Object> params) {
        FinanceWalletSearchValidate search = new FinanceWalletSearchValidate();
        search.setUser_info(string(params, "user_info", "userInfo"));
        search.setChange_type(integer(params, "change_type", "changeType"));
        search.setAccount_type(string(params, "account_type", "accountType"));
        search.setStart_time(string(params, "start_time", "startTime"));
        search.setEnd_time(string(params, "end_time", "endTime"));
        return ResponseResult.success(walletService.list(page(params), search));
    }

    @PostMapping("/user/recharge/list")
    public ResponseResult<PageResult<FinanceRechargeListVo>> userRechargeList(@RequestBody Map<String, Object> params) {
        FinanceRechargeSearchValidate search = new FinanceRechargeSearchValidate();
        search.setSn(string(params, "sn"));
        search.setUser_info(string(params, "user_info", "userInfo"));
        search.setPay_way(integer(params, "pay_way", "payWay"));
        search.setPay_status(integer(params, "pay_status", "payStatus"));
        search.setStart_time(string(params, "start_time", "startTime"));
        search.setEnd_time(string(params, "end_time", "endTime"));
        return ResponseResult.success(rechargerService.list(page(params), search));
    }

    private MallRefund requireRefund(Integer id) {
        MallRefund refund = refundService.getById(id);
        if (refund == null) {
            throw new IllegalArgumentException("退款记录不存在");
        }
        return refund;
    }

    private static PageValidate page(Map<String, Object> params) {
        PageValidate page = new PageValidate();
        Integer pageNo = integer(params, "page_no", "page");
        Integer pageSize = integer(params, "page_size", "size");
        if (pageNo != null && pageNo > 0) page.setPage_no(pageNo);
        if (pageSize != null && pageSize > 0) page.setPage_size(pageSize);
        return page;
    }

    private static MallRefundSearchValidate refundSearch(Map<String, Object> params) {
        MallRefundSearchValidate search = new MallRefundSearchValidate();
        search.setShopId(integer(params, "shopId", "shop_id"));
        search.setSn(string(params, "sn"));
        search.setFrom_sn(string(params, "from_sn", "fromSn"));
        search.setNickname(string(params, "nickname", "userName"));
        search.setType(integer(params, "type"));
        search.setStatus(integer(params, "status"));
        search.setStart_time(string(params, "start_time", "startTime"));
        search.setEnd_time(string(params, "end_time", "endTime"));
        return search;
    }

    static String string(Map<String, Object> params, String... keys) {
        for (String key : keys) {
            Object value = params.get(key);
            if (value != null && !value.toString().isBlank()) return value.toString().trim();
        }
        return null;
    }

    static Integer integer(Map<String, Object> params, String... keys) {
        String value = string(params, keys);
        return value == null ? null : Integer.valueOf(value);
    }

    private static BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
