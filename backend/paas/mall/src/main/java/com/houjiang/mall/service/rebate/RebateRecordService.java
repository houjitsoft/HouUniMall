package com.houjiang.mall.service.rebate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.rebate.RebateRecord;
import com.houjiang.mall.mapper.rebate.RebateRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 返利记录服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class RebateRecordService extends ServiceImpl<RebateRecordMapper, RebateRecord> {

    /**
     * 分页查询返利记录
     * 
     * @param params 查询参数
     * @return 分页数据
     */
    public IPage<RebateRecord> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String rebateNo = (String) params.get("rebateNo");
        String orderNo = (String) params.get("orderNo");
        Integer rebateType = params.get("rebateType") != null ? Integer.parseInt(params.get("rebateType").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Long userId = params.get("userId") != null ? Long.parseLong(params.get("userId").toString()) : null;

        QueryWrapper<RebateRecord> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.hasText(rebateNo), "rebate_no", rebateNo)
               .like(StringUtils.hasText(orderNo), "order_no", orderNo)
               .eq(rebateType != null, "rebate_type", rebateType)
               .eq(status != null, "status", status)
               .eq(shopId != null, "shop_id", shopId)
               .eq(userId != null, "user_id", userId)
               .orderByDesc("create_time");

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建返利记录
     * 
     * @param record 记录信息
     * @return 是否成功
     */
    public Boolean createRecord(RebateRecord record) {
        record.setRebateNo(generateRebateNo());
        // 待结算
        record.setStatus(0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        return save(record);
    }

    /**
     * 结算返利记录
     * 
     * @param id 记录ID
     * @return 是否成功
     */
    public Boolean settle(Long id) {
        RebateRecord record = new RebateRecord();
        record.setId(id);
        // 已结算
        record.setStatus(1);
        record.setSettleTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        return updateById(record);
    }

    /**
     * 批量结算返利记录
     * 
     * @param ids 记录ID列表
     * @return 是否成功
     */
    public Boolean batchSettle(List<Long> ids) {
        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            RebateRecord record = new RebateRecord();
            record.setId(id);
            record.setStatus(1);
            record.setSettleTime(now);
            record.setUpdateTime(now);
            updateById(record);
        }
        return true;
    }

    /**
     * 取消返利记录
     * 
     * @param id 记录ID
     * @param reason 取消原因
     * @return 是否成功
     */
    public Boolean cancel(Long id, String reason) {
        RebateRecord record = new RebateRecord();
        record.setId(id);
        // 已取消
        record.setStatus(2);
        record.setInvalidReason(reason);
        record.setUpdateTime(LocalDateTime.now());
        return updateById(record);
    }

    /**
     * 生成返利单号
     * 
     * @return 返利单号
     */
    private String generateRebateNo() {
        return "RB" + System.currentTimeMillis();
    }
}
