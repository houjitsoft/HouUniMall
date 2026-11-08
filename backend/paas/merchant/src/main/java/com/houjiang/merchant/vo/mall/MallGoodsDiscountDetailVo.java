package com.houjiang.merchant.vo.mall;

import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.pt.GoodsPtVo;
import com.houjiang.merchant.vo.user.DiscountUserListedVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGoodsDiscountDetailVo implements Serializable {

    @Schema(description = "ID")
    private Integer id;
    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品货号")
    private String code;

    @Schema(description = "主图")
    private String image;

    @Schema(description = "折扣规则 0默认 1单独折扣 2单独价格")
    private Integer discountType;

    @Schema(description = "会员折扣 1是 0否")
    private Integer isDiscount;

    @Schema(description = "用户等级列表")
    private List<DiscountUserListedVo> userLevel;
}
