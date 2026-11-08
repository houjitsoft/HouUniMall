package com.houjiang.commons.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description:
 * @CreateDate: 2025/7/7 20:57
 * @author Amos
 */
@UtilityClass
public class PointsRatioUtil {

    /**
     * 根据商品支付比例以及汇率计算商品的三个价格：劲豆、嘉币、现金
     *
     * @param goodsPrice  商品价格
     * @param pointsRate  积分比例
     * @return
     */
    public static PointsCashRatioRes getPointsRatio(BigDecimal goodsPrice, Double pointsRate) {
        // 计算汇率价格
        // 根据设置比例，计算商品价格
        BigDecimal pointsPrice = goodsPrice.multiply(new BigDecimal(pointsRate / 100))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        // 根据汇率计算嘉币商品价格

        // 根据汇率计算劲豆商品价格

        // 设置现金金额
        return new PointsCashRatioRes(goodsPrice.subtract(pointsPrice));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointsCashRatioRes {
        private BigDecimal cashPrice;
    }
}
