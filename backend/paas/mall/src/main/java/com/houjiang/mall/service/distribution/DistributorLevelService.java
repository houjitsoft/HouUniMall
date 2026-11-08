package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.DistributorLevel;
import com.houjiang.mall.mapper.distribution.DistributorLevelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分销等级服务类
 * 
 * @author Amos
 */
@Service
public class DistributorLevelService extends ServiceImpl<DistributorLevelMapper, DistributorLevel> {

    /**
     * 获取店铺的分销等级列表
     *
     * @param shopId 店铺ID
     * @return 等级列表
     */
    public List<DistributorLevel> listByShop(Long shopId) {
        LambdaQueryWrapper<DistributorLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, DistributorLevel::getShopId, shopId);
        wrapper.eq(DistributorLevel::getStatus, 1);
        wrapper.orderByAsc(DistributorLevel::getSort);
        return this.list(wrapper);
    }

    /**
     * 创建分销等级
     *
     * @param level 等级信息
     * @return 是否成功
     */
    public boolean createLevel(DistributorLevel level) {
        level.setStatus(1);
        level.setCreateTime(LocalDateTime.now());
        level.setUpdateTime(LocalDateTime.now());
        return this.save(level);
    }

    /**
     * 获取默认等级
     *
     * @param shopId 店铺ID
     * @return 默认等级
     */
    public DistributorLevel getDefaultLevel(Long shopId) {
        LambdaQueryWrapper<DistributorLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistributorLevel::getShopId, shopId);
        wrapper.eq(DistributorLevel::getIsDefault, 1);
        return this.getOne(wrapper);
    }
}
