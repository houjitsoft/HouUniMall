package com.houjiang.mall.controller.user;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.user.ShoppingCart;
import com.houjiang.mall.service.user.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车Controller
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/mall/cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public ResponseResult<Boolean> addToCart(@RequestBody ShoppingCart cart) {
        if (cart.getUserId() == null || cart.getSkuId() == null || cart.getQuantity() == null
                || cart.getQuantity() <= 0) {
            return ResponseResult.error("参数不完整");
        }
        boolean result = shoppingCartService.addToCart(cart);
        return ResponseResult.success(result);
    }

    /**
     * 更新购物车商品数量
     */
    @PostMapping("/update-quantity")
    public ResponseResult<Boolean> updateQuantity(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        Long userId = Long.valueOf(param.get("userId").toString());
        Integer quantity = Integer.valueOf(param.get("quantity").toString());
        boolean result = shoppingCartService.updateQuantity(id, userId, quantity);
        return ResponseResult.success(result);
    }

    /**
     * 切换选中状态
     */
    @PostMapping("/toggle-selected")
    public ResponseResult<Boolean> toggleSelected(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        Long userId = Long.valueOf(param.get("userId").toString());
        Integer selected = Integer.valueOf(param.get("selected").toString());
        boolean result = shoppingCartService.toggleSelected(id, userId, selected);
        return ResponseResult.success(result);
    }

    /**
     * 全选/全不选
     */
    @PostMapping("/select-all")
    public ResponseResult<Boolean> selectAll(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        Integer selected = Integer.valueOf(param.get("selected").toString());
        boolean result = shoppingCartService.selectAll(userId, selected);
        return ResponseResult.success(result);
    }

    /**
     * 删除购物车商品
     */
    @PostMapping("/remove")
    public ResponseResult<Boolean> removeFromCart(@RequestBody Map<String, Object> param) {
        Long id = Long.valueOf(param.get("id").toString());
        Long userId = Long.valueOf(param.get("userId").toString());
        boolean result = shoppingCartService.removeFromCart(id, userId);
        return ResponseResult.success(result);
    }

    /**
     * 批量删除购物车商品
     */
    @PostMapping("/batch-remove")
    public ResponseResult<Boolean> batchRemove(@RequestBody Map<String, Object> param) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) param.get("ids");
        Long userId = Long.valueOf(param.get("userId").toString());
        boolean result = shoppingCartService.batchRemove(ids, userId);
        return ResponseResult.success(result);
    }

    /**
     * 清空购物车
     */
    @PostMapping("/clear")
    public ResponseResult<Boolean> clearCart(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        boolean result = shoppingCartService.clearCart(userId);
        return ResponseResult.success(result);
    }

    /**
     * 获取购物车列表
     */
    @PostMapping("/list")
    public ResponseResult<List<ShoppingCart>> getCartList(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        List<ShoppingCart> list = shoppingCartService.getCartList(userId);
        return ResponseResult.success(list);
    }

    /**
     * 获取购物车数量
     */
    @PostMapping("/count")
    public ResponseResult<Long> getCartCount(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        long count = shoppingCartService.getCartCount(userId);
        return ResponseResult.success(count);
    }

    /**
     * 获取已选中的购物车商品
     */
    @PostMapping("/selected")
    public ResponseResult<List<ShoppingCart>> getSelectedCarts(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        List<ShoppingCart> list = shoppingCartService.getSelectedCarts(userId);
        return ResponseResult.success(list);
    }

    /**
     * 按店铺分组获取购物车
     */
    @PostMapping("/group-by-shop")
    public ResponseResult<Map<Long, List<ShoppingCart>>> getCartGroupByShop(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        Map<Long, List<ShoppingCart>> map = shoppingCartService.getCartGroupByShop(userId);
        return ResponseResult.success(map);
    }
}
