package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.ShoppingCartVO;
import com.houjiang.order.entity.OrderUserCartEntity;
import io.seata.core.exception.TransactionException;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName OrderUserCartService
 * @Description 购物车表
 * @Author Amos
 * @Date 2023-05-25 13:39:41
 * @Version 1.0
 * @author Amos
 */
public interface OrderUserCartService extends IService<OrderUserCartEntity> {
    /**
     * 查询商品在购物车的数量
     * @param userId
     * @param goodsId
     * @return
     */
    Integer selectCurrentCartQuantity(String userId, Integer goodsId);

    /**
     * 购物车  修改商品数量
     * @param map
     * @return
     */
    ResponseResult changeGoodsNum(Map<String, Object> map);

    /**
     * 购物车  批量删除
     * @param map
     * @return
     */
    ResponseResult batchDeleteGoods(Map<String, Object> map);

    /**
     * 查询购物车商品并分组
     *
     * @param map
     * @return
     */
    ResponseResult queryShoppingCart(Map<String, Object> map);

    /**
     * 删除购物车无效商品并分组
     *
     * @param map
     * @return
     */
    ResponseResult deleteInvalidProductByCart(Map<String, Object> map);

    /**
     * 插入购物车
     *
     * @param map
     * @return
     */
    ResponseResult insertShoppingCart(Map<String, Object> map) throws TransactionException;


    /**
     *平台查看用户购物车
/**
     * @param param
     * @return
     */
    ResponseResult getShoppongCartList(ShoppingCartVO param) throws IllegalAccessException;


    /**
     * 更新购物车 除shopId无其他条件
     *
     * @param map
     * @return
     */
    ResponseResult updateOrderUserCarByMap(Map<String, Object> map);


    /**
     * 热门推荐商品 插入购物车
     *
     * @param map
     * @return
     */
    ResponseResult insertShoppingCartBatch(Map<String, Object> map);

    /**
     * 删除商品数量
     *
     * @param map
     * @return
     */
    ResponseResult reduceGoodsNum(Map<String, Object> map);


    /**
     * 添加商品数量
     *
     * @param map
     * @return
     */
    ResponseResult addGoodsNum(Map<String, Object> map);


    /**
     * 删除购物车
     * @param shopId
     */
    void deleteUserCat(String shopId);
}

