package com.houjiang.base.feign;


import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("activity")
public interface ActivityClient {

    /**
     * 查询红包模板信息
     *
     * @param paraMap
     * @return
     */
    @PostMapping(value = "/activity/redpacketTemplate/list")
    ResponseResult redpacketTemplateList(@RequestBody Map<String, Object> paraMap);

    /**
     * 查询活动商品价格 限时折扣
     */
    @PostMapping(value = "/activity/discountGoods/query/goodsPrice")
    ResponseResult selectPriceByGoodsId(@RequestParam Integer goodsId);

    /**
     *查询活动商品价格 限时秒杀
     */
    @PostMapping(value = "/activity/seckillGoods/seckillGoodsPrice")
    ResponseResult seckillGoodsPrice(@RequestParam Integer goodsId);

    /**
     * 查询活动商品价格 拼团
     */
    @PostMapping(value = "/activity/piecingTogetherMark/query/goodsPrice")
    ResponseResult pinTuanGoodsPrice(@RequestParam Integer goodsId);

    /**
     * 查询活动商品价格 满减赠折
     */
    @PostMapping(value = "/activity/reductionPolicyBase/selectPrice")
    ResponseResult manJianZengZheGoodsPrice(@RequestParam Integer goodsId);
}
