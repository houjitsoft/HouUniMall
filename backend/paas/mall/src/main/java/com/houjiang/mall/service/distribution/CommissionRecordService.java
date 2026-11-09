package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.CommissionRecord;
import com.houjiang.mall.entity.distribution.Distributor;
import com.houjiang.mall.mapper.distribution.CommissionRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 佣金记录服务类
 *
 * 资金口径（与 DistributionSettleService / DistributionWithdrawService 严格配对）：
 *   生成佣金：pending += amount，total += amount
 *   到期结算 / 后台手工结算：pending -= amount，available += amount
 *
 * 后台「佣金记录」页的手工结算与定时任务必须走同一口径，否则会出现
 * 「记录已显示已结算，但分销员可提现余额没有增加」的账实不符，进而导致提现被拒。
 *
 * @author Amos
 */
@Service
public class CommissionRecordService extends ServiceImpl<CommissionRecordMapper, CommissionRecord> {

    @Resource
    private DistributorService distributorService;

    /**
     * 分页查询佣金记录
     *
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<CommissionRecord> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Long distributorId = params.get("distributorId") != null ? Long.parseLong(params.get("distributorId").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(distributorId != null, CommissionRecord::getDistributorId, distributorId);
        wrapper.eq(shopId != null, CommissionRecord::getShopId, shopId);
        wrapper.eq(status != null, CommissionRecord::getStatus, status);
        wrapper.orderByDesc(CommissionRecord::getCreateTime);

        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 创建佣金记录
     *
     * @param record 佣金记录
     * @return 是否成功
     */
    public boolean createRecord(CommissionRecord record) {
        record.setRecordNo(generateRecordNo());
        // 待结算
        record.setStatus(0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        return this.save(record);
    }

    /**
     * 手工结算单条佣金：状态流转 + 资金台账联动，口径与到期自动结算一致
     *
     * @param id 记录ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean settle(Long id) {
        CommissionRecord record = this.getById(id);
        if (record == null || record.getStatus() == null || record.getStatus() != 0) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        record.setStatus(1);
        record.setSettleTime(now);
        record.setUpdateTime(now);
        if (!this.updateById(record)) {
            return false;
        }
        movePendingToAvailable(record.getDistributorId(),
                record.getCommissionAmount() == null ? BigDecimal.ZERO : record.getCommissionAmount());
        return true;
    }

    /**
     * 批量手工结算：单事务，全部成功才返回 true
     *
     * @param ids 记录ID列表
     * @return 是否全部成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSettle(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        int settled = 0;
        for (Long id : ids) {
            if (id != null && settle(id)) {
                settled++;
            }
        }
        return settled == ids.size();
    }

    /**
     * 待结算 → 可提现。与 DistributionSettleService.settleDueRecords 使用同一段 setSql，
     * 保证手工结算与到期自动结算不会把台账算成两个口径。
     */
    private void movePendingToAvailable(Long distributorId, BigDecimal commission) {
        if (distributorId == null || commission == null || commission.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        distributorService.lambdaUpdate()
                .eq(Distributor::getId, distributorId)
                .setSql("pending_commission = pending_commission - " + commission.toPlainString()
                        + ", available_commission = available_commission + " + commission.toPlainString())
                .update();
    }

    /**
     * 统计分销员佣金
     *
     * @param distributorId 分销员ID
     * @return 统计结果
     */
    public Map<String, BigDecimal> statsCommission(Long distributorId) {
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommissionRecord::getDistributorId, distributorId);
        // 已结算
        wrapper.eq(CommissionRecord::getStatus, 1);

        BigDecimal total = this.list(wrapper).stream()
                .map(CommissionRecord::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of("totalSettled", total);
    }

    private String generateRecordNo() {
        return "CR" + System.currentTimeMillis();
    }
}
