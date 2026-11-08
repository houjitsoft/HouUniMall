package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.CommissionRecord;
import com.houjiang.mall.mapper.distribution.CommissionRecordMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 佣金记录服务类
 * 
 * @author Amos
 */
@Service
public class CommissionRecordService extends ServiceImpl<CommissionRecordMapper, CommissionRecord> {

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
     * 结算佣金
     *
     * @param id 记录ID
     * @return 是否成功
     */
    public boolean settle(Long id) {
        CommissionRecord record = this.getById(id);
        if (record == null || record.getStatus() != 0) {
            return false;
        }
        record.setStatus(1);
        record.setSettleTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        return this.updateById(record);
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
