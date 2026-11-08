package com.houjiang.mall.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.user.ShoppingCart;
import com.houjiang.mall.mapper.user.ShoppingCartMapper;
import com.houjiang.mall.feign.MallGoodsClient;
import com.houjiang.commons.dto.mall.MallSkuSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 购物车服务实现类
 * 
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartService extends ServiceImpl<ShoppingCartMapper, ShoppingCart> {

    private final MallGoodsClient goodsClient;

    /**
     * 添加商品到购物车
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addToCart(ShoppingCart cart) {
        MallSkuSnapshot snapshot = validateSku(cart.getSkuId(), cart.getQuantity());
        applySnapshot(cart, snapshot);
        // 检查是否已存在相同商品
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, cart.getUserId())
                .eq(ShoppingCart::getGoodsId, cart.getGoodsId())
                .eq(cart.getSkuId() != null, ShoppingCart::getSkuId, cart.getSkuId());
        
        ShoppingCart existing = this.getOne(wrapper);
        if (existing != null) {
            // 如果已存在,增加数量
            existing.setQuantity(existing.getQuantity() + cart.getQuantity());
            existing.setSelected(1);
            return this.updateById(existing);
        } else {
            // 不存在则新增
            cart.setSelected(1);
            cart.setIsValid(1);
            return this.save(cart);
        }
    }

    /**
     * 更新购物车商品数量
     */
    public boolean updateQuantity(Long id, Long userId, Integer quantity) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getId, id)
                .eq(ShoppingCart::getUserId, userId);
        
        ShoppingCart cart = this.getOne(wrapper);
        if (cart == null) {
            return false;
        }
        MallSkuSnapshot snapshot = validateSku(cart.getSkuId(), quantity);
        cart.setQuantity(quantity);
        applySnapshot(cart, snapshot);
        return this.updateById(cart);
    }

    /**
     * 切换选中状态
     */
    public boolean toggleSelected(Long id, Long userId, Integer selected) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getId, id)
                .eq(ShoppingCart::getUserId, userId);
        
        ShoppingCart cart = this.getOne(wrapper);
        if (cart == null) {
            return false;
        }
        
        cart.setSelected(selected);
        return this.updateById(cart);
    }

    /**
     * 全选/全不选
     */
    public boolean selectAll(Long userId, Integer selected) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        
        List<ShoppingCart> carts = this.list(wrapper);
        carts.forEach(cart -> cart.setSelected(selected));
        return this.updateBatchById(carts);
    }

    /**
     * 删除购物车商品
     */
    public boolean removeFromCart(Long id, Long userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getId, id)
                .eq(ShoppingCart::getUserId, userId);
        return this.remove(wrapper);
    }

    /**
     * 批量删除购物车商品
     */
    public boolean batchRemove(List<Long> ids, Long userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ShoppingCart::getId, ids)
                .eq(ShoppingCart::getUserId, userId);
        return this.remove(wrapper);
    }

    /**
     * 清空购物车
     */
    public boolean clearCart(Long userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        return this.remove(wrapper);
    }

    /**
     * 获取用户购物车列表
     */
    public List<ShoppingCart> getCartList(Long userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> carts = this.list(wrapper);
        boolean changed = false;
        for (ShoppingCart cart : carts) {
            MallSkuSnapshot snapshot = cart.getSkuId() == null
                    ? invalidSnapshot("请选择商品规格") : goodsClient.requireSaleableSku(cart.getSkuId());
            changed |= applySnapshot(cart, snapshot);
        }
        if (changed) {
            this.updateBatchById(carts);
        }
        return carts;
    }

    /**
     * 获取用户购物车数量
     */
    public long getCartCount(Long userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        return this.count(wrapper);
    }

    /**
     * 获取已选中的购物车商品
     */
    public List<ShoppingCart> getSelectedCarts(Long userId) {
        return getCartList(userId).stream()
                .filter(cart -> Integer.valueOf(1).equals(cart.getSelected()))
                .filter(cart -> Integer.valueOf(1).equals(cart.getIsValid()))
                .toList();
    }

    /**
     * 按店铺分组获取购物车
     */
    public Map<Long, List<ShoppingCart>> getCartGroupByShop(Long userId) {
        List<ShoppingCart> carts = getCartList(userId);
        return carts.stream()
                .collect(java.util.stream.Collectors.groupingBy(ShoppingCart::getShopId));
    }

    private MallSkuSnapshot validateSku(Long skuId, Integer quantity) {
        if (skuId == null) {
            throw new IllegalArgumentException("SKU不能为空");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("购物车数量必须大于零");
        }
        MallSkuSnapshot snapshot = goodsClient.requireSaleableSku(skuId);
        if (snapshot == null || !snapshot.isSaleable()) {
            throw new IllegalStateException(snapshot == null ? "SKU不存在" : snapshot.getInvalidReason());
        }
        if (snapshot.getSaleableStock() == null || snapshot.getSaleableStock() < quantity) {
            throw new IllegalStateException("SKU库存不足");
        }
        return snapshot;
    }

    private boolean applySnapshot(ShoppingCart cart, MallSkuSnapshot snapshot) {
        boolean saleable = snapshot != null && snapshot.isSaleable()
                && snapshot.getSaleableStock() != null
                && snapshot.getSaleableStock() >= (cart.getQuantity() == null ? 0 : cart.getQuantity());
        cart.setIsValid(saleable ? 1 : 0);
        cart.setInvalidReason(saleable ? null : invalidReason(snapshot));
        if (snapshot != null) {
            cart.setGoodsId(snapshot.getGoodsId());
            cart.setShopId(snapshot.getShopId());
            cart.setGoodsName(snapshot.getGoodsName());
            cart.setGoodsImage(snapshot.getGoodsImage());
            cart.setSkuSpec(snapshot.getSkuSpec());
            cart.setPrice(snapshot.getSalePrice());
        }
        return true;
    }

    private String invalidReason(MallSkuSnapshot snapshot) {
        if (snapshot == null) return "SKU不存在";
        if (!snapshot.isSaleable()) return snapshot.getInvalidReason();
        return "SKU库存不足";
    }

    private MallSkuSnapshot invalidSnapshot(String reason) {
        MallSkuSnapshot snapshot = new MallSkuSnapshot();
        snapshot.setSaleable(false);
        snapshot.setInvalidReason(reason);
        return snapshot;
    }
}
