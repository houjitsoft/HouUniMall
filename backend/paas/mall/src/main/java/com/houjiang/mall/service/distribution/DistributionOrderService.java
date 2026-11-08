package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.DistributionOrder;
import com.houjiang.mall.mapper.distribution.DistributionOrderMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 分销订单服务类
 * 
 * @author Amos
 */
@Service
public class DistributionOrderService extends ServiceImpl<DistributionOrderMapper, DistributionOrder> {

    /**
     * 分页查询分销订单列表
     *
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<DistributionOrder> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Long distributorId = params.get("distributorId") != null ? Long.parseLong(params.get("distributorId").toString()) : null;
        Integer orderStatus = params.get("orderStatus") != null ? Integer.parseInt(params.get("orderStatus").toString()) : null;
        String orderNo = (String) params.get("orderNo");

        LambdaQueryWrapper<DistributionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, DistributionOrder::getShopId, shopId);
        wrapper.eq(distributorId != null, DistributionOrder::getDistributorId, distributorId);
        wrapper.eq(orderStatus != null, DistributionOrder::getOrderStatus, orderStatus);
        wrapper.like(orderNo != null, DistributionOrder::getOrderNo, orderNo);
        wrapper.orderByDesc(DistributionOrder::getCreateTime);

        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 创建分销订单
     *
     * @param order 分销订单信息
     * @return 是否成功
     */
    public boolean createOrder(DistributionOrder order) {
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return this.save(order);
    }

    /**
     * 更新分销订单状态
     *
     * @param id 订单ID
     * @param status 状态
     * @return 是否成功
     */
    public boolean updateStatus(Long id, Integer status) {
        DistributionOrder order = new DistributionOrder();
        order.setId(id);
        order.setOrderStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    /**
     * 根据分销员ID统计订单数量
     *
     * @param distributorId 分销员ID
     * @return 订单数量
     */
    public Long countByDistributor(Long distributorId) {
        LambdaQueryWrapper<DistributionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistributionOrder::getDistributorId, distributorId);
        return this.count(wrapper);
    }
}
