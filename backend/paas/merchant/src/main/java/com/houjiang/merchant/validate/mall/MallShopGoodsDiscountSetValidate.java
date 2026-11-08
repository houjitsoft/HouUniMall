package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import com.houjiang.merchant.vo.mall.MallGoodsSkuVo;
import com.houjiang.merchant.vo.mall.SkuNameListVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 商品参数
 * @author Amos
 */
@Data
@Schema(description = "商品是否参与折扣参数")
public class MallShopGoodsDiscountSetValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "ids参数缺失")
    @Schema(description = "ID")
    private List<Integer> ids;

    @Schema(description = "是否参与折扣")
    private Integer isDiscount;
}
