package com.houjiang.mall.service.rebate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.rebate.RebateAdjustment;
import com.houjiang.mall.mapper.rebate.RebateAdjustmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 返利调整服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class RebateAdjustmentService extends ServiceImpl<RebateAdjustmentMapper, RebateAdjustment> {

    /**
     * 分页查询返利调整
     * 
     * @param params 查询参数
     * @return 分页数据
     */
    public IPage<RebateAdjustment> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String adjustNo = (String) params.get("adjustNo");
        Integer adjustType = params.get("adjustType") != null ? Integer.parseInt(params.get("adjustType").toString()) : null;
        Integer auditStatus = params.get("auditStatus") != null ? Integer.parseInt(params.get("auditStatus").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Long userId = params.get("userId") != null ? Long.parseLong(params.get("userId").toString()) : null;

        LambdaQueryWrapper<RebateAdjustment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(adjustNo), RebateAdjustment::getAdjustNo, adjustNo)
               .eq(adjustType != null, RebateAdjustment::getAdjustType, adjustType)
               .eq(auditStatus != null, RebateAdjustment::getAuditStatus, auditStatus)
               .eq(shopId != null, RebateAdjustment::getShopId, shopId)
               .eq(userId != null, RebateAdjustment::getUserId, userId)
               .orderByDesc(RebateAdjustment::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建返利调整
     * 
     * @param adjustment 调整信息
     * @return 是否成功
     */
    public Boolean createAdjustment(RebateAdjustment adjustment) {
        adjustment.setAdjustNo(generateAdjustNo());
        // 待审核
        adjustment.setAuditStatus(0);
        adjustment.setCreateTime(LocalDateTime.now());
        adjustment.setUpdateTime(LocalDateTime.now());
        return save(adjustment);
    }

    /**
     * 审核返利调整
     * 
     * @param id 调整ID
     * @param auditStatus 审核状态
     * @param auditorId 审核人ID
     * @param auditorName 审核人姓名
     * @param auditRemark 审核备注
     * @return 是否成功
     */
    public Boolean audit(Long id, Integer auditStatus, Long auditorId, String auditorName, String auditRemark) {
        RebateAdjustment adjustment = new RebateAdjustment();
        adjustment.setId(id);
        adjustment.setAuditStatus(auditStatus);
        adjustment.setAuditorId(auditorId);
        adjustment.setAuditorName(auditorName);
        adjustment.setAuditRemark(auditRemark);
        adjustment.setAuditTime(LocalDateTime.now());
        adjustment.setUpdateTime(LocalDateTime.now());
        return updateById(adjustment);
    }

    /**
     * 生成调整单号
     * 
     * @return 调整单号
     */
    private String generateAdjustNo() {
        return "ADJ" + System.currentTimeMillis();
    }
}
