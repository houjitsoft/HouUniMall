package com.houjiang.merchant.validate.gather;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGatherSaveGoodsValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "必须提交ID")
    @Schema(description = "id")
    private List<Integer> ids;

    @NotNull(message = "商品采集状态")
    @Schema(description = "商品采集状态")
    @IntegerContains(values = {0, 1}, message = "商品采集状态错误")
    private Integer goodsStatus;

    @Schema(description = "店铺ID")
    private Integer shopId;
}
