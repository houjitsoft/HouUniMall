package com.houjiang.mall.controller.finance;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.finance.FinanceExpense;
import com.houjiang.mall.entity.finance.FinanceIncome;
import com.houjiang.mall.entity.finance.FinanceSettlement;
import com.houjiang.mall.feign.MerchantPlatformFinanceFeign;
import com.houjiang.mall.service.finance.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 财务管理Controller
 * 包含概览(1) + 收入(4) + 支出(4) + 结算(4) + 提现(4) = 共17个端点
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/finance")
@RequiredArgsConstructor
public class FinanceController {

    private static final String CENTER_READ = "hasAnyAuthority('*','mall:finance:center','mall:finance:mall','shell:mall:finance:overview')";
    private static final String REFUND_READ = "hasAnyAuthority('*','mall:finance:refund')";
    private static final String SHOP_BALANCE_READ = "hasAnyAuthority('*','mall:finance:shop_balance')";
    private static final String USER_WALLET_READ = "hasAnyAuthority('*','mall:finance:user_balance','mall:finance:user_commission')";
    private static final String USER_RECHARGE_READ = "hasAnyAuthority('*','mall:finance:user_deposit')";
    private static final String INCOME_READ = "hasAnyAuthority('*','mall:finance:mall','shell:mall:finance:income')";
    private static final String EXPENSE_READ = "hasAnyAuthority('*','mall:finance:mall','shell:mall:finance:expense')";
    private static final String SETTLEMENT_READ = "hasAnyAuthority('*','mall:finance:shop_settle','mall:finance:shop_settlement')";
    private static final String WITHDRAW_READ = "hasAnyAuthority('*','mall:finance:shop_withdraw','mall:finance:user_withdraw')";

    private final FinanceService financeService;
    private final MerchantPlatformFinanceFeign merchantPlatformFinanceFeign;

    // ========== 财务概览 ==========

    @PostMapping("/center")
    @PreAuthorize(CENTER_READ)
    public ResponseResult merchantFinanceCenter() {
        return merchantPlatformFinanceFeign.center(Map.of());
    }

    @PostMapping("/refund/stats")
    @PreAuthorize(REFUND_READ)
    public ResponseResult refundStats(@RequestBody(required = false) Map<String, Object> params) {
        return merchantPlatformFinanceFeign.refundStats(params == null ? Map.of() : params);
    }

    @PostMapping("/refund/list")
    @PreAuthorize(REFUND_READ)
    public ResponseResult refundList(@RequestBody Map<String, Object> params) {
        return merchantPlatformFinanceFeign.refundList(params);
    }

    @PostMapping("/refund/retry")
    @PreAuthorize("hasAnyAuthority('*','mall:finance:refund:retry','shell:mall:finance:refund:retry')")
    public ResponseResult retryRefund(@RequestBody Map<String, Object> params) {
        return merchantPlatformFinanceFeign.retryRefund(params);
    }

    @PostMapping("/shop/balance/list")
    @PreAuthorize(SHOP_BALANCE_READ)
    public ResponseResult shopBalanceList(@RequestBody Map<String, Object> params) {
        return merchantPlatformFinanceFeign.shopBalanceList(params);
    }

    @PostMapping("/user/wallet/list")
    @PreAuthorize(USER_WALLET_READ)
    public ResponseResult userWalletList(@RequestBody Map<String, Object> params, Authentication authentication) {
        String accountType = resolveScope(params, "account_type", authentication,
                "mall:finance:user_balance", "mall:finance:user_commission", "balance", "commission");
        Map<String, Object> scoped = new HashMap<>(params);
        scoped.put("account_type", accountType);
        return merchantPlatformFinanceFeign.userWalletList(scoped);
    }

    @PostMapping("/user/recharge/list")
    @PreAuthorize(USER_RECHARGE_READ)
    public ResponseResult userRechargeList(@RequestBody Map<String, Object> params) {
        return merchantPlatformFinanceFeign.userRechargeList(params);
    }

    /**
     * 获取财务概览统计数据
     *
     * @return ResponseResult 含totalIncome/totalExpense/netIncome/pendingSettlement/pendingWithdraw字段
     */
    @PostMapping("/overview")
    @PreAuthorize(CENTER_READ)
    public ResponseResult overview() {
        try {
            return ResponseResult.success(financeService.overview());
        } catch (Exception e) {
            log.error("获取财务概览失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    // ========== 收入 ==========

    /**
     * 分页查询收入列表
     *
     * @param params 查询参数
     * @return ResponseResult
     */
    @PostMapping("/income/list")
    @PreAuthorize(INCOME_READ)
    public ResponseResult incomeList(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(financeService.incomePageList(params));
        } catch (Exception e) {
            log.error("查询收入列表失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取收入详情
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/income/detail")
    @PreAuthorize(INCOME_READ)
    public ResponseResult incomeDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            FinanceIncome income = financeService.getIncome(id);
            return income != null ? ResponseResult.success(income) : ResponseResult.fail(404, "记录不存在");
        } catch (Exception e) {
            log.error("查询收入详情失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建收入记录
     *
     * @param income 收入实体
     * @return ResponseResult
     */
    @PostMapping("/income/create")
    @PreAuthorize("hasAnyAuthority('*','mall:finance:income:create','shell:mall:finance:income:create')")
    public ResponseResult incomeCreate(@RequestBody FinanceIncome income) {
        try {
            return financeService.createIncome(income) ? ResponseResult.success("创建成功") : ResponseResult.fail(500, "创建失败");
        } catch (Exception e) {
            log.error("创建收入记录失败", e);
            return ResponseResult.fail(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 确认收入记录
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/income/confirm")
    @PreAuthorize("hasAnyAuthority('*','mall:finance:income:confirm','shell:mall:finance:income:confirm')")
    public ResponseResult incomeConfirm(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return financeService.confirmIncome(id) ? ResponseResult.success("确认成功") : ResponseResult.fail(500, "确认失败");
        } catch (Exception e) {
            log.error("确认收入记录失败", e);
            return ResponseResult.fail(500, "确认失败: " + e.getMessage());
        }
    }

    // ========== 支出 ==========

    /**
     * 分页查询支出列表
     *
     * @param params 查询参数
     * @return ResponseResult
     */
    @PostMapping("/expense/list")
    @PreAuthorize(EXPENSE_READ)
    public ResponseResult expenseList(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(financeService.expensePageList(params));
        } catch (Exception e) {
            log.error("查询支出列表失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取支出详情
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/expense/detail")
    @PreAuthorize(EXPENSE_READ)
    public ResponseResult expenseDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            FinanceExpense expense = financeService.getExpense(id);
            return expense != null ? ResponseResult.success(expense) : ResponseResult.fail(404, "记录不存在");
        } catch (Exception e) {
            log.error("查询支出详情失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建支出记录
     *
     * @param expense 支出实体
     * @return ResponseResult
     */
    @PostMapping("/expense/create")
    @PreAuthorize("hasAnyAuthority('*','mall:finance:expense:create','shell:mall:finance:expense:create')")
    public ResponseResult expenseCreate(@RequestBody FinanceExpense expense) {
        try {
            return financeService.createExpense(expense) ? ResponseResult.success("创建成功") : ResponseResult.fail(500, "创建失败");
        } catch (Exception e) {
            log.error("创建支出记录失败", e);
            return ResponseResult.fail(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 确认支出记录
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/expense/confirm")
    @PreAuthorize("hasAnyAuthority('*','mall:finance:expense:confirm','shell:mall:finance:expense:confirm')")
    public ResponseResult expenseConfirm(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return financeService.confirmExpense(id) ? ResponseResult.success("确认成功") : ResponseResult.fail(500, "确认失败");
        } catch (Exception e) {
            log.error("确认支出记录失败", e);
            return ResponseResult.fail(500, "确认失败: " + e.getMessage());
        }
    }

    // ========== 结算 ==========

    /**
     * 分页查询结算列表
     *
     * @param params 查询参数
     * @return ResponseResult
     */
    @PostMapping("/settlement/list")
    @PreAuthorize(SETTLEMENT_READ)
    public ResponseResult settlementList(@RequestBody Map<String, Object> params) {
        try {
            return ResponseResult.success(financeService.settlementPageList(params));
        } catch (Exception e) {
            log.error("查询结算列表失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取结算详情
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/settlement/detail")
    @PreAuthorize(SETTLEMENT_READ)
    public ResponseResult settlementDetail(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            FinanceSettlement settlement = financeService.getSettlement(id);
            return settlement != null ? ResponseResult.success(settlement) : ResponseResult.fail(404, "记录不存在");
        } catch (Exception e) {
            log.error("查询结算详情失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建结算记录
     *
     * @param settlement 结算实体
     * @return ResponseResult
     */
    @PostMapping("/settlement/create")
    @PreAuthorize("hasAnyAuthority('*','mall:finance:settlement:create','shell:mall:finance:settlement:create')")
    public ResponseResult settlementCreate(@RequestBody FinanceSettlement settlement) {
        try {
            return financeService.createSettlement(settlement) ? ResponseResult.success("创建成功") : ResponseResult.fail(500, "创建失败");
        } catch (Exception e) {
            log.error("创建结算记录失败", e);
            return ResponseResult.fail(500, "创建失败: " + e.getMessage());
        }
    }

    /**
     * 执行结算
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/settlement/settle")
    @PreAuthorize("hasAnyAuthority('*','mall:finance:shop_settle:settle','shell:mall:finance:settlement:settle')")
    public ResponseResult settlementSettle(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return financeService.settle(id) ? ResponseResult.success("结算成功") : ResponseResult.fail(500, "结算失败");
        } catch (Exception e) {
            log.error("执行结算失败", e);
            return ResponseResult.fail(500, "结算失败: " + e.getMessage());
        }
    }

    // ========== 提现 ==========

    /**
     * 分页查询提现列表
     *
     * @param params 查询参数：page,size,shopName,memberName,settlementNo,status
     * @return ResponseResult
     */
    @PostMapping("/withdraw/list")
    @PreAuthorize(WITHDRAW_READ)
    public ResponseResult withdrawList(@RequestBody Map<String, Object> params, Authentication authentication) {
        String accountType = resolveWithdrawReadScope(params, authentication);
        try {
            return ResponseResult.success(financeService.withdrawPageList(params, accountType));
        } catch (Exception e) {
            log.error("查询提现列表失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取提现详情
     *
     * @param params 包含id字段
     * @return ResponseResult
     */
    @PostMapping("/withdraw/detail")
    @PreAuthorize(WITHDRAW_READ)
    public ResponseResult withdrawDetail(@RequestBody Map<String, Object> params, Authentication authentication) {
        String accountType = resolveWithdrawReadScope(params, authentication);
        try {
            Long id = Long.parseLong(params.get("id").toString());
            return ResponseResult.success(financeService.getWithdraw(id, accountType));
        } catch (Exception e) {
            log.error("查询提现详情失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 提现审核（通过/拒绝）
     *
     * @param params 包含id, result(approved/rejected), remark字段
     * @return ResponseResult
     */
    @PostMapping("/withdraw/audit")
    @PreAuthorize("hasAnyAuthority('*','mall:finance:shop_withdraw:audit','mall:finance:user_withdraw:audit','shell:mall:finance:withdraw:audit')")
    public ResponseResult withdrawAudit(@RequestBody Map<String, Object> params, Authentication authentication) {
        String accountType = resolveScope(params, "accountType", authentication,
                "mall:finance:shop_withdraw:audit", "mall:finance:user_withdraw:audit", "shop", "user");
        try {
            Long id = Long.parseLong(params.get("id").toString());
            String result = (String) params.get("result");
            String remark = (String) params.getOrDefault("remark", "");
            return financeService.auditWithdraw(id, result, remark, accountType)
                    ? ResponseResult.success("审核成功")
                    : ResponseResult.fail(500, "审核失败，请确认该提现申请存在且处于待审核状态");
        } catch (Exception e) {
            log.error("提现审核失败", e);
            return ResponseResult.fail(500, "审核失败: " + e.getMessage());
        }
    }

    /**
     * 获取提现统计数据
     *
     * @return ResponseResult 含pending/approved/rejected/totalAmount字段
     */
    @PostMapping("/withdraw/stats")
    @PreAuthorize(WITHDRAW_READ)
    public ResponseResult withdrawStats(@RequestBody(required = false) Map<String, Object> params,
                                        Authentication authentication) {
        Map<String, Object> safeParams = params == null ? Map.of() : params;
        String accountType = resolveWithdrawReadScope(safeParams, authentication);
        try {
            return ResponseResult.success(financeService.withdrawStats(accountType));
        } catch (Exception e) {
            log.error("获取提现统计失败", e);
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }

    private static String resolveWithdrawReadScope(Map<String, Object> params, Authentication authentication) {
        return resolveScope(params, "accountType", authentication,
                "mall:finance:shop_withdraw", "mall:finance:user_withdraw", "shop", "user");
    }

    static String resolveScope(Map<String, Object> params, String parameterName, Authentication authentication,
                               String firstPermission, String secondPermission,
                               String firstValue, String secondValue) {
        boolean unrestricted = hasAuthority(authentication, "*");
        boolean firstAllowed = unrestricted || hasAuthority(authentication, firstPermission);
        boolean secondAllowed = unrestricted || hasAuthority(authentication, secondPermission);
        Object raw = params.get(parameterName);
        String requested = raw == null ? null : raw.toString().trim();
        if (requested == null || requested.isEmpty()) {
            if (firstAllowed && !secondAllowed) return firstValue;
            if (secondAllowed && !firstAllowed) return secondValue;
            throw new AccessDeniedException("必须指定有权限的财务账户类型");
        }
        if (firstValue.equals(requested) && firstAllowed) return firstValue;
        if (secondValue.equals(requested) && secondAllowed) return secondValue;
        throw new AccessDeniedException("无权访问该财务账户类型");
    }

    private static boolean hasAuthority(Authentication authentication, String authority) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(item -> authority.equals(item.getAuthority()));
    }
}
