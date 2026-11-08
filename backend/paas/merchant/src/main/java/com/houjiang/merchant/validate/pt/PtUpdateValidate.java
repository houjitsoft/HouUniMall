package com.houjiang.merchant.validate.pt;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.vo.ms.MsCUSkuListedVo;
import com.houjiang.merchant.vo.pt.PtCUSkuListedVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class PtUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID", required = true)
    private Integer id;
    @Schema(description = "商品ID")
    @NotNull(message = "请选择商品")
    private Integer goodsId;

    @Schema(description = "拼团失效时间min")
    @NotNull(message = "请填写拼团失效有效期")
    private Integer timeLimit;

    @Schema(description = "拼团商品人数")
    @NotNull(message = "请填写拼团商品人数")
    private Integer userNum;

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
    @NotNull(message = "请选择拼团sku")
    private List<PtCUSkuListedVo> ptSkuList;
}
