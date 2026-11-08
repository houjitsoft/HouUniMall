package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.entity.ActivityDiscountBaseEntity;

import java.util.Map;

/**
 * 限时折扣服务接口
 *
 * @author Amos
 * @date 2026-01-04
 */
public interface DiscountService extends IService<ActivityDiscountBaseEntity> {

    /**
     * 分页查询限时折扣列表
     */
    Map<String, Object> queryDiscountList(Map<String, Object> param);

    /**
     * 查询限时折扣详情
     */
    Map<String, Object> getDiscountDetail(Map<String, Object> param);

    /**
     * 新增限时折扣
     */
    boolean addDiscount(Map<String, Object> discountData);

    /**
     * 更新限时折扣
     */
    boolean updateDiscount(Map<String, Object> discountData);

    /**
     * 删除限时折扣
     */
    boolean deleteDiscount(Map<String, Object> param);

    /**
     * 启动限时折扣
     */
    boolean startDiscount(Map<String, Object> param);

    /**
     * 查询折扣商品列表
     */
    Map<String, Object> queryDiscountGoods(Map<String, Object> param);
}
