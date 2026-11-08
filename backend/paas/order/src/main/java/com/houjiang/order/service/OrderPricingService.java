package com.houjiang.order.service;

import com.houjiang.commons.dto.mall.MallSkuSnapshot;
import com.houjiang.order.feign.GoodServiceClient;
import com.houjiang.order.plugin.CartCalcGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class OrderPricingService {

    private final GoodServiceClient goodsClient;

    /**
     * 二开购物车算价网关；为 {@code null} 时（缺省装配 / 单测）算价走官方路径。
     */
    private final CartCalcGateway cartCalcGateway;

    /**
     * 单参构造：兼容无二开网关的装配与既有单测，行为等价于「无二开」。
     */
    public OrderPricingService(GoodServiceClient goodsClient) {
        this(goodsClient, null);
    }

    @Autowired
    public OrderPricingService(GoodServiceClient goodsClient, CartCalcGateway cartCalcGateway) {
        this.goodsClient = goodsClient;
        this.cartCalcGateway = cartCalcGateway;
    }

    public PricingResult price(List<PricingItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("订单商品不能为空");
        }
        List<PricedLine> lines = new ArrayList<>(items.size());
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (PricingItem item : items) {
            if (item == null || item.skuId() == null) {
                throw new IllegalArgumentException("SKU不能为空");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException("购买数量必须大于零");
            }
            MallSkuSnapshot sku = goodsClient.requireSaleableSku(item.skuId());
            if (sku == null || !sku.isSaleable()) {
                throw new IllegalStateException(sku == null ? "SKU不存在" : sku.getInvalidReason());
            }
            if (sku.getSalePrice() == null || sku.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("SKU售价无效");
            }
            if (sku.getSaleableStock() == null || sku.getSaleableStock() < item.quantity()) {
                throw new IllegalStateException("SKU库存不足");
            }
            BigDecimal unitPrice = sku.getSalePrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineAmount = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));
            goodsAmount = goodsAmount.add(lineAmount);
            lines.add(new PricedLine(sku, item.quantity(), unitPrice, lineAmount));
        }
        BigDecimal roundedGoodsAmount = goodsAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal payAmount = roundedGoodsAmount;
        // 二开购物车算价（after*）：官方商品总额算完后回调 CartCalcExtension，回读折扣与应付金额。
        // 领域夹取（折扣 ∈ [0, goodsAmount]，payAmount = goodsAmount - discount）在网关内完成。
        if (cartCalcGateway != null) {
            CartCalcGateway.Result calcResult = cartCalcGateway.calc(null, roundedGoodsAmount, Map.of());
            discountAmount = calcResult.discountAmount();
            payAmount = calcResult.payAmount();
        }
        return new PricingResult(roundedGoodsAmount, List.copyOf(lines), discountAmount, payAmount);
    }

    public record PricingItem(Long skuId, Integer quantity, BigDecimal clientPrice) {}

    public record PricedLine(MallSkuSnapshot sku, Integer quantity,
                             BigDecimal unitPrice, BigDecimal lineAmount) {}

    public record PricingResult(BigDecimal goodsAmount, List<PricedLine> lines,
                                BigDecimal discountAmount, BigDecimal payAmount) {}
}
