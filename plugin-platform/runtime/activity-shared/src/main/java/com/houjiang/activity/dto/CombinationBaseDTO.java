package com.houjiang.activity.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * (ActivityCombinationBase)表实体类
 *
 * @author Amos
 * @since 2024-04-16 11:26:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombinationBaseDTO extends PageHelper {

    /**
     * 组合活动id
     */
    private Integer combinationId;

    /**
    * 组合活动名称
    */
    @NotNull(message = "组合活动名称不能为空")
    private String combinationName;
        
    /**
    * 组合活动类型 1固定组合 2搭配组合 
    */
    @NotNull(message = "组合活动类型不能为空")
    @Range(min = 1,max = 2,message = "组合活动类型传值范围0~1")
    private Integer combinationType;
        
    /**
    * 开始时间
    */
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
        
    /**
    * 结束时间
    */
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
        
    /**
    * 组合活动状态  0-未开始;1-已开始;2-已结束；3-已失效
    */
    private Integer combinationStatus;
        
    /**
    * 组合活动价格
    */
    @NotNull(message = "组合活动价格不能为空")
    private BigDecimal combinationPrice;
        
    /**
    * 组合活动主图
    */
    @NotNull(message = "组合活动主图不能为空")
    private String combinationImage;
        
    /**
    * 运费模板id
    */
    @NotNull(message = "运费模板不能为空")
    private Integer transportTemplateId;

    /**
     * 售卖区域id
     */
    @NotNull(message = "售卖区域不能为空")
    private Integer transportAreaId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 搭配价格
     */
    private BigDecimal matchingPrices;

    /**
     * 商品列表
     */
    @NotNull(message = "组合商品列表不能为空")
    private List<CombinationGoodsDTO> goodsList;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 商品ID集合
     */
    private List<Integer> goodsIdList;

}
