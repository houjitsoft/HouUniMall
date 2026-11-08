package com.houjiang.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.activity.entity.ActivityDiscountBaseEntity;
import com.houjiang.activity.mapper.ActivityDiscountBaseMapper;
import com.houjiang.activity.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 限时折扣服务实现类
 *
 * @author Amos
 * @date 2026-01-04
 */
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl extends ServiceImpl<ActivityDiscountBaseMapper, ActivityDiscountBaseEntity> implements DiscountService {

    @Override
    public Map<String, Object> queryDiscountList(Map<String, Object> param) {
        LambdaQueryWrapper<ActivityDiscountBaseEntity> wrapper = new LambdaQueryWrapper<>();

        if (param.containsKey("discountName") && StringUtils.hasText(String.valueOf(param.get("discountName")))) {
            wrapper.like(ActivityDiscountBaseEntity::getDiscountName, param.get("discountName"));
        }

        if (param.containsKey("discountState") && param.get("discountState") != null) {
            wrapper.eq(ActivityDiscountBaseEntity::getDiscountState, param.get("discountState"));
        }

        wrapper.orderByDesc(ActivityDiscountBaseEntity::getDiscountCreateTime);

        int page = param.containsKey("page") ? Integer.parseInt(String.valueOf(param.get("page"))) : 1;
        int size = param.containsKey("size") ? Integer.parseInt(String.valueOf(param.get("size"))) : 10;
        Page<ActivityDiscountBaseEntity> pageParam = new Page<>(page, size);
        IPage<ActivityDiscountBaseEntity> pageResult = this.page(pageParam, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());

        return result;
    }

    @Override
    public Map<String, Object> getDiscountDetail(Map<String, Object> param) {
        if (!param.containsKey("discountId")) {
            throw new RuntimeException("折扣活动ID不能为空");
        }

        Integer discountId = Integer.parseInt(String.valueOf(param.get("discountId")));
        ActivityDiscountBaseEntity discount = this.getById(discountId);
        if (discount == null) {
            throw new RuntimeException("折扣活动不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("discount", discount);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addDiscount(Map<String, Object> discountData) {
        ActivityDiscountBaseEntity discount = new ActivityDiscountBaseEntity();
        discount.setDiscountName(String.valueOf(discountData.get("discountName")));
        discount.setShopName(String.valueOf(discountData.get("shopName")));
        discount.setShopId(1);
        discount.setUserId("1");
        discount.setUserNickName("管理员");
        discount.setDiscountState(0);
        discount.setDiscountCreateTime(new java.util.Date());

        return this.save(discount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDiscount(Map<String, Object> discountData) {
        if (!discountData.containsKey("discountId")) {
            throw new RuntimeException("折扣活动ID不能为空");
        }

        Integer discountId = Integer.parseInt(String.valueOf(discountData.get("discountId")));
        ActivityDiscountBaseEntity discount = this.getById(discountId);
        if (discount == null) {
            throw new RuntimeException("折扣活动不存在");
        }

        discount.setDiscountName(String.valueOf(discountData.get("discountName")));
        discount.setShopName(String.valueOf(discountData.get("shopName")));

        return this.updateById(discount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDiscount(Map<String, Object> param) {
        if (!param.containsKey("discountId")) {
            throw new RuntimeException("折扣活动ID不能为空");
        }

        Integer discountId = Integer.parseInt(String.valueOf(param.get("discountId")));
        return this.removeById(discountId);
    }

    @Override
    public boolean startDiscount(Map<String, Object> param) {
        if (!param.containsKey("discountId")) {
            throw new RuntimeException("折扣活动ID不能为空");
        }

        Integer discountId = Integer.parseInt(String.valueOf(param.get("discountId")));
        ActivityDiscountBaseEntity discount = this.getById(discountId);
        if (discount == null) {
            throw new RuntimeException("折扣活动不存在");
        }

        discount.setDiscountState(1);
        return this.updateById(discount);
    }

    @Override
    public Map<String, Object> queryDiscountGoods(Map<String, Object> param) {
        // 这里返回模拟数据，实际应查询hou_addon_activity_discount_goods表
        Map<String, Object> result = new HashMap<>();
        result.put("goods", new ArrayList<>());
        return result;
    }
}
