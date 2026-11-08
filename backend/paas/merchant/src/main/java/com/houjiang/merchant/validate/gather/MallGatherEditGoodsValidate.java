package com.houjiang.merchant.validate.gather;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallGatherEditGoodsValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "必须提交ID")
    @Schema(description = "id")
    private Integer id;

    @NotNull(message = "商品采集信息")
    @Schema(description = "商品采集信息")
    private JSONObject content;
}
