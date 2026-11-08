package com.houjiang.activity.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.activity.dto.CombinationGoodsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * (ActivityCombinationBase)表实体类
 *
 * @author Amos
 * @since 2024-04-16 11:26:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombinationPcVO {

    /**
     * 组合活动id
     */
    private Integer combinationId;

    /**
    * 组合活动名称
    */
    private String combinationName;
        
    /**
    * 组合活动类型 1固定组合 2搭配组合 
    */
    private Integer combinationType;
        
    /**
    * 开始时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
        
    /**
    * 结束时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
        
    /**
    * 组合活动状态  0-未开始;1-已开始;2-已结束；3-已失效
    */
    private Integer combinationStatus;
        
    /**
    * 组合活动价格
    */
    private BigDecimal combinationPrice;
        
    /**
    * 组合活动主图
    */
    private String combinationImage;

    /**
     * 商品原价
     */
    private BigDecimal goodsOriginalPrice;

    /**
     * 店铺Id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 组合活动商品列表
     */
    private List<CombinationGoodsDTO> goodsList;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 固定组合 最小商品库存
     */
    private Integer goodsStock;

    /**
     * 是否开具增值税发票 1是，0否
     */
    private Integer shopInvoices;

    /**
     * 发票
     */
    private Integer invoiceActive;

    /**
     * 最大购买数量
     */
    private Integer maxBuyNum;
}
