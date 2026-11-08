package com.houjiang.merchant.vo.user;

import com.houjiang.merchant.vo.mall.MallGoodsUserLevelDiscountVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class DiscountUserListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Integer id;
    @Schema(description = "用户等级")
    private String name;
    @Schema(description = "等级")
    private Integer level;
    @Schema(description = "是否默认等级")
    private Integer isDefault;
    @Schema(description = "等级折扣 1参与等级折扣 0无等级折扣")
    private Integer isDiscount;
    @Schema(description = "等级折扣比例")
    private BigDecimal discountRatio;

    @Schema(description = "优惠列表")
    private List<MallGoodsUserLevelDiscountVo> discountList;
}
