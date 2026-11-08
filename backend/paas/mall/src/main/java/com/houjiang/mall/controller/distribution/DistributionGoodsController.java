package com.houjiang.mall.controller.distribution;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.distribution.DistributionGoods;
import com.houjiang.mall.service.distribution.DistributionGoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分销商品控制器
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/distribution/goods")
@RequiredArgsConstructor
public class DistributionGoodsController {

    private final DistributionGoodsService distributionGoodsService;

    /**
     * 分页查询分销商品
     */
    @PostMapping("/list")
    public ResponseResult<IPage<DistributionGoods>> list(@RequestBody Map<String, Object> params) {
        return ResponseResult.success(distributionGoodsService.pageList(params));
    }

    /**
     * 获取启用的分销商品列表
     */
    @PostMapping("/enabled")
    public ResponseResult<List<DistributionGoods>> listEnabled(@RequestBody Map<String, Object> params) {
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        return ResponseResult.success(distributionGoodsService.listEnabled(shopId));
    }

    /**
     * 获取分销商品详情
     */
    @PostMapping("/detail")
    public ResponseResult<DistributionGoods> detail(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(distributionGoodsService.getById(id));
    }

    /**
     * 根据商品ID获取分销商品
     */
    @PostMapping("/byGoods")
    public ResponseResult<DistributionGoods> getByGoodsId(@RequestBody Map<String, Object> params) {
        Long goodsId = Long.parseLong(params.get("goodsId").toString());
        return ResponseResult.success(distributionGoodsService.getByGoodsId(goodsId));
    }

    /**
     * 创建分销商品
     */
    @PostMapping("/create")
    public ResponseResult<Boolean> create(@RequestBody DistributionGoods goods) {
        return ResponseResult.success(distributionGoodsService.createGoods(goods));
    }

    /**
     * 更新分销商品
     */
    @PostMapping("/update")
    public ResponseResult<Boolean> update(@RequestBody DistributionGoods goods) {
        return ResponseResult.success(distributionGoodsService.updateGoods(goods));
    }

    /**
     * 更新分销商品状态
     */
    @PostMapping("/status")
    public ResponseResult<Boolean> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        return ResponseResult.success(distributionGoodsService.updateStatus(id, status));
    }

    /**
     * 删除分销商品
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        return ResponseResult.success(distributionGoodsService.removeById(id));
    }
}
