package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

/**
 * @ClassName SpuIdAndPriceDTO
 * @Description
 * @Author Amos
 * @Date 2024/12/10 15:39
 * @Version 1.0
 * @author Amos
 */
@Data
public class SpuIdAndPriceDTO {

    /**
     * 商品SPU ID
     * 用于唯一标识一个商品的SPU（Standard Product Unit），即商品的基本信息单元
     */
    private String spuId;

    /**
     * 商品SKU ID
     * 用于唯一标识一个商品的SKU（Stock Keeping Unit），即库存单元，代表商品的不同属性组合
     */
    private String skuId;

    /**
     * 原始分销价格
     * 记录商品在分销渠道中的原始定价，用于跟踪价格变化和计算价格差异
     */
    private String orgiDistriPrice;

    /**
     * 当前分销价格
     * 商品在分销渠道中的当前实际销售价格，可能因促销、折扣等因素而变化
     */
    private String distriPrice;

    /**
     * 官方分销价格
     * 由官方设定的分销价格，用于统一各分销渠道的价格标准，维护市场价格秩序
     */
    private String officialDistriPrice;

    /**
     * 原始建议零售价
     * 记录商品的原始建议零售价，作为定价参考和历史价格数据分析的基础
     */
    private String orgiSuggestPrice;

    /**
     * 当前建议零售价
     * 商品的当前建议零售价，可能根据市场策略和活动调整，指导零售商定价
     */
    private String suggestPrice;

}
