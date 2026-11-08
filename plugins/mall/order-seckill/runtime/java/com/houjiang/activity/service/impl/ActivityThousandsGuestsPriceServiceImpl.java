package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.dto.ThousandsGuestsPriceConfigDTO;
import com.houjiang.activity.dto.ThousandsGuestsPriceListQueryDTO;
import com.houjiang.activity.entity.ActivityThousandsGuestsPriceEntity;
import com.houjiang.activity.mapper.ActivityThousandsGuestsPriceMapper;
import com.houjiang.activity.service.ActivityThousandsGuestsPriceService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 千客千价价格策略 Service 实现
 * @author Amos
 */
@Service
public class ActivityThousandsGuestsPriceServiceImpl extends ServiceImpl<ActivityThousandsGuestsPriceMapper, ActivityThousandsGuestsPriceEntity>
        implements ActivityThousandsGuestsPriceService {

    @Override
    public ResponseResult queryConfigPage(ThousandsGuestsPriceListQueryDTO param) {
        int page = param.getPage() == null ? 1 : param.getPage();
        int size = param.getSize() == null ? 10 : param.getSize();

        LambdaQueryWrapper<ActivityThousandsGuestsPriceEntity> wrapper = new LambdaQueryWrapper<>();
        if (param.getShopId() != null) {
            wrapper.eq(ActivityThousandsGuestsPriceEntity::getShopId, param.getShopId());
        }
        if (param.getGoodsId() != null) {
            wrapper.eq(ActivityThousandsGuestsPriceEntity::getGoodsId, param.getGoodsId());
        }
        if (param.getSegmentType() != null) {
            wrapper.eq(ActivityThousandsGuestsPriceEntity::getSegmentType, param.getSegmentType());
        }
        if (param.getStatus() != null) {
            wrapper.eq(ActivityThousandsGuestsPriceEntity::getStatus, param.getStatus());
        }
        wrapper.orderByDesc(ActivityThousandsGuestsPriceEntity::getUpdateTime);

        Page<ActivityThousandsGuestsPriceEntity> pageParam = new Page<>(page, size);
        IPage<ActivityThousandsGuestsPriceEntity> pageResult = this.page(pageParam, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());

        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult saveConfig(ThousandsGuestsPriceConfigDTO param, UserInfoVO userInfo) {
        Date now = new Date();
        ActivityThousandsGuestsPriceEntity entity;
        if (param.getId() != null) {
            entity = this.getById(param.getId());
            if (entity == null) {
                return ResponseResult.error("价格策略不存在");
            }
        } else {
            entity = new ActivityThousandsGuestsPriceEntity();
            entity.setCreateBy(userInfo.getUserName());
            entity.setCreateTime(now);
        }

        entity.setShopId(param.getShopId());
        entity.setGoodsId(param.getGoodsId());
        entity.setSkuId(param.getSkuId());
        entity.setSegmentType(param.getSegmentType());
        entity.setSegmentValue(param.getSegmentValue());
        entity.setPriceType(param.getPriceType());
        entity.setPriceValue(param.getPriceValue());
        entity.setPriority(param.getPriority());
        entity.setStatus(param.getStatus());
        if (param.getStartTime() != null) {
            entity.setStartTime(new Date(param.getStartTime()));
        } else {
            entity.setStartTime(null);
        }
        if (param.getEndTime() != null) {
            entity.setEndTime(new Date(param.getEndTime()));
        } else {
            entity.setEndTime(null);
        }
        entity.setRemark(param.getRemark());

        entity.setUpdateBy(userInfo.getUserName());
        entity.setUpdateTime(now);

        this.saveOrUpdate(entity);
        return ResponseResult.success(entity.getId());
    }

    @Override
    public ResponseResult deleteConfig(Long id) {
        if (id == null) {
            return ResponseResult.error("ID 不能为空");
        }
        this.removeById(id);
        return ResponseResult.success(null);
    }

    @Override
    public ResponseResult updateStatus(Long id, Integer status, UserInfoVO userInfo) {
        if (id == null || status == null) {
            return ResponseResult.error("ID 和状态不能为空");
        }
        ActivityThousandsGuestsPriceEntity entity = this.getById(id);
        if (entity == null) {
            return ResponseResult.error("价格策略不存在");
        }
        entity.setStatus(status);
        entity.setUpdateBy(userInfo.getUserName());
        entity.setUpdateTime(new Date());
        this.updateById(entity);
        return ResponseResult.success(null);
    }

    @Override
    public ResponseResult queryPersonalPrice(Map<String, Object> param) {
        if (param == null || !param.containsKey("goodsId") || !param.containsKey("basePrice")) {
            return ResponseResult.error("goodsId 和 basePrice 不能为空");
        }
        Long goodsId = Long.parseLong(String.valueOf(param.get("goodsId")));
        Long skuId = param.get("skuId") != null ? Long.parseLong(String.valueOf(param.get("skuId"))) : null;
        String userId = param.get("userId") != null ? String.valueOf(param.get("userId")) : null;
        BigDecimal basePrice = new BigDecimal(String.valueOf(param.get("basePrice")));

        Date now = new Date();

        // 优先：按用户ID匹配的规则
        ActivityThousandsGuestsPriceEntity strategy = findBestStrategy(goodsId, skuId, 1, userId, now);
        // 其次：按全部用户的规则
        if (strategy == null) {
            strategy = findBestStrategy(goodsId, skuId, 0, null, now);
        }

        if (strategy == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("originalPrice", basePrice);
            result.put("personalPrice", basePrice);
            result.put("strategyId", null);
            return ResponseResult.success(result);
        }

        BigDecimal personalPrice = calculatePrice(basePrice, strategy);

        Map<String, Object> result = new HashMap<>();
        result.put("originalPrice", basePrice);
        result.put("personalPrice", personalPrice);
        result.put("strategyId", strategy.getId());
        result.put("priceType", strategy.getPriceType());
        result.put("priceValue", strategy.getPriceValue());
        result.put("segmentType", strategy.getSegmentType());
        result.put("segmentValue", strategy.getSegmentValue());
        return ResponseResult.success(result);
    }

    private ActivityThousandsGuestsPriceEntity findBestStrategy(Long goodsId,
                                                                Long skuId,
                                                                Integer segmentType,
                                                                String segmentValue,
                                                                Date now) {
        LambdaQueryWrapper<ActivityThousandsGuestsPriceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityThousandsGuestsPriceEntity::getGoodsId, goodsId)
                .eq(ActivityThousandsGuestsPriceEntity::getStatus, 1)
                .eq(ActivityThousandsGuestsPriceEntity::getSegmentType, segmentType);
        if (skuId != null) {
            wrapper.eq(ActivityThousandsGuestsPriceEntity::getSkuId, skuId);
        }
        if (segmentValue != null) {
            wrapper.eq(ActivityThousandsGuestsPriceEntity::getSegmentValue, segmentValue);
        }
        wrapper.and(w -> w.isNull(ActivityThousandsGuestsPriceEntity::getStartTime)
                .or().le(ActivityThousandsGuestsPriceEntity::getStartTime, now));
        wrapper.and(w -> w.isNull(ActivityThousandsGuestsPriceEntity::getEndTime)
                .or().ge(ActivityThousandsGuestsPriceEntity::getEndTime, now));
        wrapper.orderByDesc(ActivityThousandsGuestsPriceEntity::getPriority)
                .orderByDesc(ActivityThousandsGuestsPriceEntity::getUpdateTime);

        List<ActivityThousandsGuestsPriceEntity> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    private BigDecimal calculatePrice(BigDecimal basePrice, ActivityThousandsGuestsPriceEntity strategy) {
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }
        if (strategy == null || strategy.getPriceType() == null || strategy.getPriceValue() == null) {
            return basePrice;
        }
        BigDecimal value = strategy.getPriceValue();
        switch (strategy.getPriceType()) {
            case 1:
                // 指定价格
                return value.max(BigDecimal.ZERO);
            case 2:
                // 折扣百分比
                BigDecimal rate = value.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                return basePrice.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            case 3:
                // 减金额
                BigDecimal result = basePrice.subtract(value);
                if (result.compareTo(BigDecimal.ZERO) < 0) {
                    result = BigDecimal.ZERO;
                }
                return result.setScale(2, RoundingMode.HALF_UP);
            default:
                return basePrice;
        }
    }
}
