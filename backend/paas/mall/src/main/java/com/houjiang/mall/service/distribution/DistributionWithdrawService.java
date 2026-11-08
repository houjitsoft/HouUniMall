package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.DistributionSetting;
import com.houjiang.mall.entity.distribution.DistributionWithdraw;
import com.houjiang.mall.entity.distribution.Distributor;
import com.houjiang.mall.mapper.distribution.DistributionWithdrawMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 分销提现服务类
 *
 * 资金口径（与结算任务严格配对，避免账实不符）：
 *   申请 → 冻结：available -= amount
 *   驳回 → 退回：available += amount
 *   完成 → 归档：withdrawn += amount
 *
 * @author Amos
 */
@Service
public class DistributionWithdrawService extends ServiceImpl<DistributionWithdrawMapper, DistributionWithdraw> {

    private final DistributionSettingService distributionSettingService;
    private final DistributorService distributorService;

    public DistributionWithdrawService(DistributionSettingService distributionSettingService,
                                       DistributorService distributorService) {
        this.distributionSettingService = distributionSettingService;
        this.distributorService = distributorService;
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
     *
     * @param withdraw 提现单（distributorId/amount 必填）
     * @return 提现单
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

        // 冻结：可提现余额扣减
        distributorService.lambdaUpdate()
                .eq(Distributor::getId, distributorId)
                .setSql("available_commission = available_commission - " + amount.toPlainString())
                .update();
        return withdraw;
    }

    /**
     * 审核提现：1=通过（待打款） 2=驳回（余额退回） 3=已打款（归档）
     *
     * @param id        提现单ID
     * @param status    目标状态
     * @param auditor   审核人
     * @param remark    审核备注
     * @return 是否成功
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
        // 幂等：已驳回/已打款不可再改
        if (withdraw.getStatus() != null && (withdraw.getStatus() == 2 || withdraw.getStatus() == 3)) {
            return false;
        }
        int from = withdraw.getStatus() == null ? 0 : withdraw.getStatus();
        withdraw.setStatus(status);
        withdraw.setAuditor(auditor);
        withdraw.setAuditRemark(remark);
        withdraw.setAuditTime(LocalDateTime.now());
        withdraw.setUpdateTime(LocalDateTime.now());
        boolean ok = this.updateById(withdraw);
        if (!ok) {
            return false;
        }

        BigDecimal amount = withdraw.getAmount() == null ? BigDecimal.ZERO : withdraw.getAmount();
        if (status == 2 && from != 2) {
            // 驳回：退回冻结金额
            distributorService.lambdaUpdate()
                    .eq(Distributor::getId, withdraw.getDistributorId())
                    .setSql("available_commission = available_commission + " + amount.toPlainString())
                    .update();
        } else if (status == 3 && from != 3) {
            // 已打款：归档到已提现
            distributorService.lambdaUpdate()
                    .eq(Distributor::getId, withdraw.getDistributorId())
                    .setSql("withdrawn_commission = withdrawn_commission + " + amount.toPlainString())
                    .update();
        }
        return true;
    }

    private String generateWithdrawNo() {
        return "DW" + System.currentTimeMillis() + (int) (Math.random() * 900 + 100);
    }
}
