package com.houjiang.merchant.validate.ms;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.vo.ms.MsCUSkuListedVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MsCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    @NotNull(message = "请选择商品")
    private Integer goodsId;

    @Schema(description = "限制数量")
    @NotNull(message = "请填写限制数量")
    private Integer orderLimit;

    @Schema(description = "开始时间")
    @NotNull(message = "请选择开始时间")
    @NotEmpty(message = "请选择开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    @NotNull(message = "请选择结束时间")
    @NotEmpty(message = "请选择结束时间")
    private String endTime;

    @Schema(description = "商品ID")
    private Integer shopId;

    @Schema(description = "sku")
    @NotNull(message = "请选择秒杀sku")
    private List<MsCUSkuListedVo> msSkuList;
}
