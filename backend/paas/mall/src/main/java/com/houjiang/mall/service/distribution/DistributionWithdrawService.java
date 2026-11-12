package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.DistributionSetting;
import com.houjiang.mall.entity.distribution.DistributionWithdraw;
import com.houjiang.mall.entity.distribution.Distributor;
import com.houjiang.mall.mapper.distribution.DistributionWithdrawMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 分销提现服务类
 *
 * 资金口径（与结算任务严格配对，避免账实相符）：
 *   申请 → 冻结：available -= amount
 *   驳回 → 退回：available += amount
 *   完成 → 归档：withdrawn += amount
 *
 * 余额自动到账（payType=4 + autoPayoutBalance=1）：审核通过时写入
 * {@code hou_pay_user_resource.user_money} 并直接完结为已打款。
 *
 * @author Amos
 */
@Slf4j
@Service
public class DistributionWithdrawService extends ServiceImpl<DistributionWithdrawMapper, DistributionWithdraw> {

    /** 到账方式：余额 */
    public static final int PAY_TYPE_BALANCE = 4;

    private final DistributionSettingService distributionSettingService;
    private final DistributorService distributorService;
    private final JdbcTemplate jdbcTemplate;

    public DistributionWithdrawService(DistributionSettingService distributionSettingService,
                                       DistributorService distributorService,
                                       JdbcTemplate jdbcTemplate) {
        this.distributionSettingService = distributionSettingService;
        this.distributorService = distributorService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 分页查询提现单
     */
    public IPage<DistributionWithdraw> pageList(Map<String, Object> params) {
        Integer page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        Integer size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        Long distributorId = params.get("distributorId") != null ? Long.parseLong(params.get("distributorId").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        LambdaQueryWrapper<DistributionWithdraw> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(distributorId != null, DistributionWithdraw::getDistributorId, distributorId);
        wrapper.eq(shopId != null, DistributionWithdraw::getShopId, shopId);
        wrapper.eq(status != null, DistributionWithdraw::getStatus, status);
        wrapper.eq(DistributionWithdraw::getDeleted, 0);
        wrapper.orderByDesc(DistributionWithdraw::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 申请提现：校验开关、门槛、可提现余额，通过后冻结余额
     */
    @Transactional(rollbackFor = Exception.class)
    public DistributionWithdraw apply(DistributionWithdraw withdraw) {
        Long distributorId = withdraw.getDistributorId();
        Distributor distributor = distributorService.getById(distributorId);
        if (distributor == null) {
            throw new IllegalArgumentException("分销员不存在");
        }
        Long shopId = withdraw.getShopId() != null ? withdraw.getShopId() : distributor.getShopId();
        DistributionSetting setting = distributionSettingService.getSetting(shopId);
        if (setting.getEnabled() == null || setting.getEnabled() != 1) {
            throw new IllegalStateException("分销功能未开启");
        }
        BigDecimal minAmount = setting.getMinWithdrawAmount() == null ? BigDecimal.ZERO : setting.getMinWithdrawAmount();
        BigDecimal amount = withdraw.getAmount() == null ? BigDecimal.ZERO : withdraw.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("提现金额必须大于 0");
        }
        if (amount.compareTo(minAmount) < 0) {
            throw new IllegalArgumentException("提现金额不得低于门槛 " + minAmount + " 元");
        }
        BigDecimal available = distributor.getAvailableCommission() == null ? BigDecimal.ZERO : distributor.getAvailableCommission();
        if (available.compareTo(amount) < 0) {
            throw new IllegalArgumentException("可提现余额不足，当前可用 " + available + " 元");
        }

        BigDecimal fee = withdraw.getFee() == null ? BigDecimal.ZERO : withdraw.getFee();
        withdraw.setShopId(shopId);
        withdraw.setUserId(distributor.getUserId());
        withdraw.setFee(fee);
        withdraw.setActualAmount(amount.subtract(fee));
        withdraw.setStatus(0);
        withdraw.setWithdrawNo(generateWithdrawNo());
        withdraw.setCreateTime(LocalDateTime.now());
        withdraw.setUpdateTime(LocalDateTime.now());
        this.save(withdraw);

        distributorService.lambdaUpdate()
                .eq(Distributor::getId, distributorId)
                .setSql("available_commission = available_commission - " + amount.toPlainString())
                .update();
        return withdraw;
    }

    /**
     * 审核提现：1=通过（待打款） 2=驳回（余额退回） 3=已打款（归档）
     * 当 status=1 且 payType=余额 且开启 autoPayoutBalance 时，自动入账并完结为 3。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Long id, Integer status, String auditor, String remark) {
        DistributionWithdraw withdraw = this.getById(id);
        if (withdraw == null) {
            return false;
        }
        if (status == null || (status != 1 && status != 2 && status != 3)) {
            throw new IllegalArgumentException("审核状态不合法");
        }
        if (withdraw.getStatus() != null && (withdraw.getStatus() == 2 || withdraw.getStatus() == 3)) {
            return false;
        }
        int from = withdraw.getStatus() == null ? 0 : withdraw.getStatus();
        Integer targetStatus = status;
        String auditRemark = remark;

        if (status == 1 && shouldAutoPayoutBalance(withdraw)) {
            creditUserBalance(withdraw);
            targetStatus = 3;
            String autoNote = "余额自动到账";
            auditRemark = (remark == null || remark.isBlank()) ? autoNote : remark + "；" + autoNote;
        }

        withdraw.setStatus(targetStatus);
        withdraw.setAuditor(auditor);
        withdraw.setAuditRemark(auditRemark);
        withdraw.setAuditTime(LocalDateTime.now());
        withdraw.setUpdateTime(LocalDateTime.now());
        boolean ok = this.updateById(withdraw);
        if (!ok) {
            return false;
        }

        BigDecimal amount = withdraw.getAmount() == null ? BigDecimal.ZERO : withdraw.getAmount();
        if (targetStatus == 2 && from != 2) {
            distributorService.lambdaUpdate()
                    .eq(Distributor::getId, withdraw.getDistributorId())
                    .setSql("available_commission = available_commission + " + amount.toPlainString())
                    .update();
        } else if (targetStatus == 3 && from != 3) {
            distributorService.lambdaUpdate()
                    .eq(Distributor::getId, withdraw.getDistributorId())
                    .setSql("withdrawn_commission = withdrawn_commission + " + amount.toPlainString())
                    .update();
        }
        return true;
    }

    private boolean shouldAutoPayoutBalance(DistributionWithdraw withdraw) {
        if (withdraw.getPayType() == null || withdraw.getPayType() != PAY_TYPE_BALANCE) {
            return false;
        }
        DistributionSetting setting = distributionSettingService.getSetting(withdraw.getShopId());
        return setting.getAutoPayoutBalance() == null || setting.getAutoPayoutBalance() == 1;
    }

    /**
     * 把实际到账金额写入用户购物余额，并记一笔资产明细（detail_type=13）。
     */
    private void creditUserBalance(DistributionWithdraw withdraw) {
        Long userId = withdraw.getUserId();
        BigDecimal credit = withdraw.getActualAmount() == null ? BigDecimal.ZERO : withdraw.getActualAmount();
        if (userId == null || credit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("余额到账失败：用户或金额无效");
        }
        String userIdStr = String.valueOf(userId);
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM hou_pay_user_resource WHERE user_id = ?",
                Integer.class, userIdStr);
        if (exists == null || exists == 0) {
            jdbcTemplate.update(
                    "INSERT INTO hou_pay_user_resource (user_id, user_money, is_delete) VALUES (?, ?, 0)",
                    userIdStr, credit);
        } else {
            jdbcTemplate.update(
                    "UPDATE hou_pay_user_resource SET user_money = user_money + ? WHERE user_id = ?",
                    credit, userIdStr);
        }
        try {
            jdbcTemplate.update(
                    "INSERT INTO hou_pay_asset_details "
                            + "(detail_type, asset_type, asset_amount, asset_direction, order_id, pay_type, user_id, detail_comment) "
                            + "VALUES (13, 1, ?, ?, ?, 1, ?, ?)",
                    credit,
                    "分销佣金提现余额",
                    withdraw.getWithdrawNo(),
                    userIdStr,
                    "分销提现自动到账 " + withdraw.getWithdrawNo());
        } catch (Exception e) {
            // 明细写入失败不阻断主账：余额已加，记日志即可
            log.warn("分销提现余额明细写入失败 withdrawNo={}: {}", withdraw.getWithdrawNo(), e.getMessage());
        }
        log.info("分销提现余额自动到账: withdrawNo={}, userId={}, amount={}",
                withdraw.getWithdrawNo(), userId, credit);
    }

    private String generateWithdrawNo() {
        return "DW" + System.currentTimeMillis() + (int) (Math.random() * 900 + 100);
    }
}
