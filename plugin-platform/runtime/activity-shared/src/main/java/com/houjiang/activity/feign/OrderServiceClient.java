package com.houjiang.activity.feign;

import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.goods.DiscountGoodsInfoVO;
import com.houjiang.commons.vo.goods.SeckillGoodsInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderServiceClient
 * @Description
 * @Author Amos
 * @Date 2023/8/10 9:34
 * @Version 1.0
 * @author Amos
 */
@FeignClient(name = "order", contextId = "activityOrderServiceClient", configuration = ActivityFeignConfig.class)
public interface OrderServiceClient {



    /**
     * 查看拼团状态
     * @param para
     * @return
     */
    @PostMapping("/order/base/checkGroupBookingStatus")
    boolean checkGroupBookingStatus(@RequestBody ExpireMarkIdUpdateDTO para);


    /**
     * 查询商品数量
     * @param param
     * @return
     */
    @PostMapping("/order/goods/feign/queryOrderGoodsCountFeign")
    Integer queryOrderGoodsCountFeign(@RequestBody  Map<String, Object> param);

    /**
     * 查询商品数量
     * @param param
     * @return
     */
    @PostMapping("/order/goods/feign/queryOrderGoodsCount")
    Integer queryOrderGoodsCount(@RequestBody  Map<String, Object> param);


    /**
     * 查询折扣商品信息，返回单个商品的详细信息
     * 该方法专注于查询单个商品的折扣信息，通过商品列表结果对象作为参数
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件和商品相关信息
     * @return 返回单个商品的折扣信息详细视图对象
     */
    @PostMapping("/order/goods/feign/queryDiscountGoodsInfo2")
    DiscountGoodsInfoVO queryDiscountGoodsInfo2(@RequestBody  DiscountGoodsListVO discountGoodsListResult);


    /**
     * 查询符合条件的买家用户列表
     * 该方法用于筛选出符合条件的买家用户，条件通过商品列表结果对象传递
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件
     * @return 返回符合条件的买家用户ID列表
     */
    @PostMapping("/order/goods/feign/queryBuyerUsers")
    List<String> queryBuyerUsers(@RequestBody DiscountGoodsListVO discountGoodsListResult);


    /**
     * 查询满足特定条件的记录数量
     * 该方法用于统计满足特定条件的记录数，这些条件通过一个参数映射传递
     *
     * @param paramMap 参数映射，包含统计查询所需的条件
     * @return 返回满足条件的记录数量
     */
    @PostMapping("/order/goods/feign/queryCountForOlder")
    Integer queryCountForOlder(@RequestBody Map<String,Object> paramMap);


    /**
     * 查询用户的订单数量
     * 该方法用于统计用户的订单数量，统计条件通过参数映射传递
     *
     * @param paramMap 参数映射，包含统计查询所需的条件
     * @return 返回用户的订单数量
     */
    @PostMapping("/order/goods/feign/queryUserOrderCount")
    Integer queryUserOrderCount(@RequestBody Map<String, Object> paramMap);


    /**
     * 更新订单的团购状态
     *
     * @param orderIdlist 一个包含多个订单ID的列表，用于指定需要更新团购状态的订单
     */
    @GetMapping("/order/base/feign/updateOrderGroupbuyStatus")
    void updateOrderGroupbuyStatus(@RequestParam("orderIdlist") List<String> orderIdlist);


    /**
     *查询商品信息
/**
     * @param appSeckillGoodsResult
     * @return
     */
    @PostMapping("/order/goods/feign/querySeckillGoodsInfo2")
    SeckillGoodsInfoVO querySeckillGoodsInfo2(@RequestBody AppSeckillGoodsQueryResultVO appSeckillGoodsResult);

}
