package com.houjiang.order.controller;


import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.ShoppingCartVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import io.seata.core.exception.TransactionException;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName OrderUserCartController
 * @Description 购物车表
 * @Author Amos
 * @Date 2023-05-25 13:39:40
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("order/userCart")
public class OrderUserCartController extends BaseController{

    /**
     * 查询商品在购物车的数量
     */
    @PostMapping("/selectCount")
    public Integer selectCurrentCartQuantity(@RequestParam("userId")String userId,@RequestParam("goodsId")Integer goodsId){
        Integer integer = orderUserCartService.selectCurrentCartQuantity(userId, goodsId);
        return integer;
    }
    /**
     * 购物车  修改商品数量
     */
    @PostMapping("/changeGoodsNum")
    public ResponseResult changeGoodsNum(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderUserCartService.changeGoodsNum(map);
        return result;
    }

    /**
     * 购物车  批量删除
     */
    @PostMapping("/batchDeleteGoods")
    public ResponseResult batchDeleteGoods(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderUserCartService.batchDeleteGoods(map);
        return result;
    }


    /**
     * 查询购物车商品并分组
     *
     * @param map
     * @return
     */
    @PostMapping("/queryShoppingCart")
    public ResponseResult queryShoppingCart(@RequestBody Map<String, Object> map) {
        try {
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
            if(userInfo != null){
            ResponseResult result = orderUserCartService.queryShoppingCart(map);
            return result;
        }
            return ResponseResult.fail("Token验证失败");
        } catch (Exception e) {
            return ResponseResult.fail("Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 删除购物车无效商品并分组
     *
     * @param map
     * @return
     */
    @PostMapping("/deleteInvalidProductByCart")
    public ResponseResult deleteInvalidProductByCart(@RequestBody Map<String, Object> map) {
        try {
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
            if(userInfo != null){
            ResponseResult result = orderUserCartService.deleteInvalidProductByCart(map);
            return result;
        }
            return ResponseResult.fail("Token验证失败");
        } catch (Exception e) {
            return ResponseResult.fail("Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 插入购物车
     *
     * @param map
     * @return
     */
    @PostMapping("/insertShoppingCart")
    public ResponseResult insertShoppingCart(@RequestBody Map<String, Object> map) throws TransactionException {

        try {
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(map.get("token").toString());
            if(userInfo != null){
            ResponseResult result = orderUserCartService.insertShoppingCart(map);
            return result;
        }
            return ResponseResult.fail("Token验证失败");
        } catch (Exception e) {
            return ResponseResult.fail("Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 热门推荐商品 插入购物车
     *
     * @param map
     * @return
     */
    @PostMapping("/business/insertShoppingCartBatch")
    public ResponseResult insertShoppingCartBatch(@RequestBody Map<String, Object> map){
        ResponseResult result = orderUserCartService.insertShoppingCartBatch(map);
        return result;
    }


    /**
     *平台查看用户购物车
/**
     * @param param
     * @return
     */
    @PostMapping("/platform/list/shoppingCart")
    public ResponseResult getShoppongCartList(@RequestBody(required = false) ShoppingCartVO param) throws IllegalAccessException {
        ResponseResult result = orderUserCartService.getShoppongCartList(param);
        return result;
    }

    /**
     * 更新购物车 除shopId无其他条件
     *
     * @param map
     * @return
     */
    @PostMapping("/updateOrderUserCar")
    public ResponseResult updateOrderUserCarByMap(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderUserCartService.updateOrderUserCarByMap(map);
        return result;
    }


    /**
     * 删除商品数量
     *
     * @param map
     * @return
     */
    @PostMapping("/h5/reduceGoodsNum")
    public ResponseResult reduceGoodsNum(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderUserCartService.reduceGoodsNum(map);
        return result;
    }

    /**
     * 添加商品数量
     *
     * @param map
     * @return
     */
    @PostMapping("/h5/addGoodsNum")
    public ResponseResult addGoodsNum(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderUserCartService.addGoodsNum(map);
        return result;

    }


    /**
     * 删除购物车
     * @param shopId
     */
    @GetMapping("/feign/deleteUserCat")
    public void deleteUserCat(@RequestParam("shopId") String shopId){
        orderUserCartService.deleteUserCat(shopId);
    }
}

