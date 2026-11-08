package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import com.houjiang.merchant.vo.user.DiscountUserListedVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 商品参数
 * @author Amos
 */
@Data
@Schema(description = "商品参与折扣参数")
public class MallShopGoodsDiscountSaveValidate implements Serializable {

    private static final long serialVersionUID = 1L;


    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;
    @Schema(description = "会员折扣 1是 0否")
    @NotNull(message = "会员折扣必传")
    @IntegerContains(values = {1, 0}, message = "会员折扣格式错误")
    private Integer isDiscount;
    @Schema(description = "折扣规则 0默认 1单独折扣 2单独价格")
    @NotNull(message = "折扣规则必传")
    @IntegerContains(values = {0,1,2}, message = "折扣规则格式错误")
    private Integer discountType;
    @Schema(description = "会员等级")
    private List<DiscountUserListedVo> userLevel;
}
