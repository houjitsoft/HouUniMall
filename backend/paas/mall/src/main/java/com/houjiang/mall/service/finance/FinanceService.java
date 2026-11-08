package com.houjiang.mall.service.finance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.mall.entity.finance.FinanceExpense;
import com.houjiang.mall.entity.finance.FinanceIncome;
import com.houjiang.mall.entity.finance.FinanceSettlement;
import com.houjiang.mall.entity.finance.FinanceWithdraw;
import com.houjiang.mall.mapper.finance.FinanceExpenseMapper;
import com.houjiang.mall.mapper.finance.FinanceIncomeMapper;
import com.houjiang.mall.mapper.finance.FinanceSettlementMapper;
import com.houjiang.mall.mapper.finance.FinanceWithdrawMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 财务管理聚合服务
 * 包含收入、支出、结算、提现的CRUD
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinanceIncomeMapper incomeMapper;
    private final FinanceExpenseMapper expenseMapper;
    private final FinanceSettlementMapper settlementMapper;
    private final FinanceWithdrawMapper withdrawMapper;

    // ========== 财务概览 ==========

    /**
     * 获取财务概览统计
     *
     * @return Map，包含totalIncome/totalExpense/pendingSettlement/pendingWithdraw等字段
     */
    public Map<String, Object> overview() {
        // 总收入
        BigDecimal totalIncome = incomeMapper.selectList(
                new LambdaQueryWrapper<FinanceIncome>().eq(FinanceIncome::getStatus, 1)
        ).stream().map(FinanceIncome::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 总支出
        BigDecimal totalExpense = expenseMapper.selectList(
                new LambdaQueryWrapper<FinanceExpense>().eq(FinanceExpense::getStatus, 1)
        ).stream().map(FinanceExpense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 待结算数
        long pendingSettlement = settlementMapper.selectCount(
                new LambdaQueryWrapper<FinanceSettlement>().eq(FinanceSettlement::getStatus, 0));

        // 待提现数
        long pendingWithdraw = withdrawMapper.selectCount(
                new LambdaQueryWrapper<FinanceWithdraw>().eq(FinanceWithdraw::getStatus, 0));

        // 已完成提现金额
        BigDecimal completedWithdrawAmount = withdrawMapper.selectList(
                new LambdaQueryWrapper<FinanceWithdraw>().eq(FinanceWithdraw::getStatus, 3)
        ).stream().map(FinanceWithdraw::getWithdrawAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("totalIncome", totalIncome);
        result.put("totalExpense", totalExpense);
        result.put("netIncome", totalIncome.subtract(totalExpense));
        result.put("pendingSettlement", pendingSettlement);
        result.put("pendingWithdraw", pendingWithdraw);
        result.put("completedWithdrawAmount", completedWithdrawAmount);
        return result;
    }

    // ========== 收入 ==========

    /**
     * 分页查询收入列表
     *
     * @param params 查询参数：page, size, type, status
     * @return 分页结果
     */
    public IPage<FinanceIncome> incomePageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<FinanceIncome> w = new LambdaQueryWrapper<>();
        if (params.get("type") != null && StringUtils.hasText(params.get("type").toString())) {
            w.eq(FinanceIncome::getType, params.get("type").toString());
        }
        if (params.get("status") != null) {
            w.eq(FinanceIncome::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        w.orderByDesc(FinanceIncome::getCreateTime);
        return incomeMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取收入详情
     *
     * @param id 收入ID
     * @return 收入详情
     */
    public FinanceIncome getIncome(Long id) {
        return incomeMapper.selectById(id);
    }

    /**
     * 创建收入记录
     *
     * @param income 收入实体
     * @return 是否成功
     */
    public boolean createIncome(FinanceIncome income) {
        return incomeMapper.insert(income) > 0;
    }

    /**
     * 确认收入记录
     *
     * @param id 收入ID
     * @return 是否成功
     */
    public boolean confirmIncome(Long id) {
        FinanceIncome i = incomeMapper.selectById(id);
        if (i == null) return false;
        i.setStatus(1);
        return incomeMapper.updateById(i) > 0;
    }

    // ========== 支出 ==========

    /**
     * 分页查询支出列表
     *
     * @param params 查询参数：page, size, type, status
     * @return 分页结果
     */
    public IPage<FinanceExpense> expensePageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<FinanceExpense> w = new LambdaQueryWrapper<>();
        if (params.get("type") != null && StringUtils.hasText(params.get("type").toString())) {
            w.eq(FinanceExpense::getType, params.get("type").toString());
        }
        if (params.get("status") != null) {
            w.eq(FinanceExpense::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        w.orderByDesc(FinanceExpense::getCreateTime);
        return expenseMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取支出详情
     *
     * @param id 支出ID
     * @return 支出详情
     */
    public FinanceExpense getExpense(Long id) {
        return expenseMapper.selectById(id);
    }

    /**
     * 创建支出记录
     *
     * @param expense 支出实体
     * @return 是否成功
     */
    public boolean createExpense(FinanceExpense expense) {
        return expenseMapper.insert(expense) > 0;
    }

    /**
     * 确认支出记录
     *
     * @param id 支出ID
     * @return 是否成功
     */
    public boolean confirmExpense(Long id) {
        FinanceExpense e = expenseMapper.selectById(id);
        if (e == null) return false;
        e.setStatus(1);
        return expenseMapper.updateById(e) > 0;
    }

    // ========== 结算 ==========

    /**
     * 分页查询结算列表
     *
     * @param params 查询参数：page, size, shopId, status
     * @return 分页结果
     */
    public IPage<FinanceSettlement> settlementPageList(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<FinanceSettlement> w = new LambdaQueryWrapper<>();
        if (params.get("shopId") != null) {
            w.eq(FinanceSettlement::getShopId, Long.parseLong(params.get("shopId").toString()));
        }
        if (params.get("status") != null) {
            w.eq(FinanceSettlement::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        if (params.get("shopName") != null && StringUtils.hasText(params.get("shopName").toString())) {
            w.like(FinanceSettlement::getShopName, params.get("shopName").toString());
        }
        w.orderByDesc(FinanceSettlement::getCreateTime);
        return settlementMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取结算详情
     *
     * @param id 结算ID
     * @return 结算详情
     */
    public FinanceSettlement getSettlement(Long id) {
        return settlementMapper.selectById(id);
    }

    /**
     * 创建结算记录
     *
     * @param settlement 结算实体
     * @return 是否成功
     */
    public boolean createSettlement(FinanceSettlement settlement) {
        return settlementMapper.insert(settlement) > 0;
    }

    /**
     * 执行结算（状态改为已结算）
     *
     * @param id 结算ID
     * @return 是否成功
     */
    public boolean settle(Long id) {
        FinanceSettlement s = settlementMapper.selectById(id);
        if (s == null) return false;
        s.setStatus(1);
        s.setSettleTime(LocalDateTime.now());
        return settlementMapper.updateById(s) > 0;
    }

    // ========== 提现 ==========

    /**
     * 分页查询提现列表
     *
     * @param params 查询参数：page, size, shopName, memberName, settlementNo, status
     * @return 分页结果
     */
    public IPage<FinanceWithdraw> withdrawPageList(Map<String, Object> params, String accountType) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        LambdaQueryWrapper<FinanceWithdraw> w = new LambdaQueryWrapper<>();
        applyWithdrawScope(w, accountType);
        if (params.get("shopName") != null && StringUtils.hasText(params.get("shopName").toString())) {
            w.like(FinanceWithdraw::getShopName, params.get("shopName").toString());
        }
        if (params.get("memberName") != null && StringUtils.hasText(params.get("memberName").toString())) {
            w.like(FinanceWithdraw::getMemberName, params.get("memberName").toString());
        }
        if (params.get("settlementNo") != null && StringUtils.hasText(params.get("settlementNo").toString())) {
            w.eq(FinanceWithdraw::getSettlementNo, params.get("settlementNo").toString());
        }
        if (params.get("status") != null && StringUtils.hasText(params.get("status").toString())) {
            // 支持字符串 pending/approved/rejected 或数字
            String statusStr = params.get("status").toString();
            int statusVal = mapWithdrawStatus(statusStr);
            if (statusVal >= 0) {
                w.eq(FinanceWithdraw::getStatus, statusVal);
            }
        }
        w.orderByDesc(FinanceWithdraw::getCreateTime);
        return withdrawMapper.selectPage(new Page<>(page, size), w);
    }

    /**
     * 获取提现详情
     *
     * @param id 提现ID
     * @return 提现详情
     */
    public FinanceWithdraw getWithdraw(Long id, String accountType) {
        LambdaQueryWrapper<FinanceWithdraw> query = new LambdaQueryWrapper<FinanceWithdraw>()
                .eq(FinanceWithdraw::getId, id);
        applyWithdrawScope(query, accountType);
        return withdrawMapper.selectOne(query);
    }

    /**
     * 提现审核（通过/拒绝）
     *
     * @param id     提现ID
     * @param result 审核结果: approved/rejected
     * @param remark 审核备注
     * @return 是否成功
     */
    public boolean auditWithdraw(Long id, String result, String remark, String accountType) {
        LambdaQueryWrapper<FinanceWithdraw> query = new LambdaQueryWrapper<FinanceWithdraw>()
                .eq(FinanceWithdraw::getId, id)
                .eq(FinanceWithdraw::getStatus, 0);
        applyWithdrawScope(query, accountType);
        FinanceWithdraw withdraw = withdrawMapper.selectOne(query);
        if (withdraw == null || withdraw.getStatus() != 0) return false;
        withdraw.setStatus("approved".equals(result) ? 1 : 2);
        withdraw.setAuditRemark(remark);
        withdraw.setAuditTime(LocalDateTime.now());
        return withdrawMapper.updateById(withdraw) > 0;
    }

    /**
     * 获取提现统计
     *
     * @return Map，包含pending/approved/rejected数量和totalAmount
     */
    public Map<String, Object> withdrawStats(String accountType) {
        long pending = withdrawMapper.selectCount(withdrawScope(0, accountType));
        long approved = withdrawMapper.selectCount(withdrawScope(1, accountType));
        long rejected = withdrawMapper.selectCount(withdrawScope(2, accountType));
        BigDecimal totalAmount = withdrawMapper.selectList(withdrawScope(null, accountType)).stream()
                .map(FinanceWithdraw::getWithdrawAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("pending", pending);
        result.put("approved", approved);
        result.put("rejected", rejected);
        result.put("totalAmount", totalAmount);
        return result;
    }

    private static LambdaQueryWrapper<FinanceWithdraw> withdrawScope(Integer status, String accountType) {
        LambdaQueryWrapper<FinanceWithdraw> query = new LambdaQueryWrapper<>();
        if (status != null) query.eq(FinanceWithdraw::getStatus, status);
        applyWithdrawScope(query, accountType);
        return query;
    }

    private static void applyWithdrawScope(LambdaQueryWrapper<FinanceWithdraw> query, String accountType) {
        if ("shop".equals(accountType)) {
            query.isNotNull(FinanceWithdraw::getShopId);
        } else if ("user".equals(accountType)) {
            query.isNotNull(FinanceWithdraw::getMemberId);
        } else {
            throw new IllegalArgumentException("无效的提现账户类型");
        }
    }

    /**
     * 提现状态字符串转数字
     *
     * @param status 字符串状态: pending/approved/rejected 或数字字符串
     * @return 数字状态，无效时返回 -1
     */
    private int mapWithdrawStatus(String status) {
        switch (status) {
            case "pending": return 0;
            case "approved": return 1;
            case "rejected": return 2;
            case "paid": return 3;
            default:
                try { return Integer.parseInt(status); } catch (NumberFormatException e) { return -1; }
        }
    }
}
