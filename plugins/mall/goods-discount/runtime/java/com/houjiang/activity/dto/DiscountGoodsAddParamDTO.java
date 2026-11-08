package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName DiscountGoodsAddParamDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/3 16:18
 * @Version 1.0
 * @author Amos
 */
@Data
public class DiscountGoodsAddParamDTO extends UserTokenBeanDTO {

    /**
     * 限时活动编号
     */
    private Integer discountId;
    /**
     * 商品集合
     */
    private List<DiscountGoodsListDTO> discountGoodsList;

    /**
     * 限时折扣商品id
     */
    private Integer discountGoodsId;
    /**
     * 活动名称
     */
    private String discountName;
    /**
     * 活动标题
     */
    private String discountTitle;
    /**
     * 活动说明
     */
    private String discountExplain;
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date goodsStartTime;
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date goodsEndTime;
    /**
     * 购买下限，0为不限制
     */
    private Integer goodsLowerLimit;
    /**
     * 状态， 1-正常/2-结束/3-管理员关闭
     */
    private Integer discountGoodsState;
    /**
     * 推荐标志 0-未推荐 1-已推荐
     */
    private Integer discountGoodsRecommend;
    /**
     * 购买上限
     */
    private Integer goodsUpperLimit;
    /**
     *  已购买的个数
     */
    private Integer goodsBuyCount;

    /**
     * 商品编号
     */
    private Integer goodsId;
    /**
     * 店铺编号
     */
    private String shopId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品原价
     */
    private BigDecimal goodsPrice;
    /**
     * 商品图片
     */
    private String goodsImage;
    private Integer commonId;

    /**
     * 限时折扣价格
     */
    private BigDecimal discountPrice;
    /**
     * 折扣
     */
    private BigDecimal discountAmount;

    /**
     * 活动预热时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date preStartTime;
    /**
     * 预热时间
     */
    private Integer preStartHour;
    /**
     * 每种商品限购 限购天数
     */
    private Integer discountUpperTime;

}
