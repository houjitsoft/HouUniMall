package com.houjiang.mall.controller.distribution;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.distribution.DistributionOrder;
import com.houjiang.mall.service.distribution.DistributionOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分销订单控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/distribution/order")
@RequiredArgsConstructor
public class DistributionOrderController {

    private final DistributionOrderService distributionOrderService;

    /**
     * 分页查询分销订单
     */
    @PostMapping("/list")
    public ResponseResult<IPage<DistributionOrder>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(distributionOrderService.pageList(params));
    }

    /**
     * 获取分销订单详情
     */
    @PostMapping("/detail")
    public ResponseResult<DistributionOrder> detail(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(distributionOrderService.getById(id));
    }

    /**
     * 创建分销订单
     */
    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody DistributionOrder order) {
        return ResponseResult.success(distributionOrderService.createOrder(order));
    }

    /**
     * 更新分销订单
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> update(@RequestBody DistributionOrder order) {
        return ResponseResult.success(distributionOrderService.updateById(order));
    }

    /**
     * 更新分销订单状态
     */
    @PostMapping("/status")
    public ResponseResult<Boolean> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        return ResponseResult.success(distributionOrderService.updateStatus(id, status));
    }

    /**
     * 删除分销订单
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(distributionOrderService.removeById(id));
    }

    /**
     * 根据分销员ID统计订单数量
     */
    @PostMapping("/count")
    public ResponseResult<Long> countByDistributor(@RequestBody Map<String, Object> params) {
        Long distributorId = Long.parseLong(params.get("distributorId").toString());
        return ResponseResult.success(distributionOrderService.countByDistributor(distributorId));
    }
}
