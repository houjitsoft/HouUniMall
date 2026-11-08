package com.houjiang.goods.controller;

import com.houjiang.commons.vo.activity.AppDiscountGoodsQueryResultVO;
import com.houjiang.goods.service.DiscountQueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 兼容旧 activity Feign 路径的限时折扣只读桥。
 * goods / 遗留调用方仍打 {@code /activity/discountGoods/*}，
 * 由本控制器落地到 goods 宿主查询（activity 服务已拆为 goods-discount 插件）。
 */
@RestController
@RequestMapping("/activity/discountGoods")
public class DiscountLegacyBridgeController {

    private final DiscountQueryService discountQueryService;

    public DiscountLegacyBridgeController(DiscountQueryService discountQueryService) {
        this.discountQueryService = discountQueryService;
    }

    @PostMapping("/selectByGoodsId")
    public AppDiscountGoodsQueryResultVO selectByGoodsId(@RequestParam("goodsId") Integer goodsId) {
        return discountQueryService.selectByGoodsId(goodsId);
    }

    @PostMapping("/selectDiscountByGoodsIdAndTime")
    public AppDiscountGoodsQueryResultVO selectDiscountByGoodsIdAndTime(@RequestParam("goodsId") Integer goodsId) {
        return discountQueryService.selectByGoodsIdAndTime(goodsId);
    }
}
