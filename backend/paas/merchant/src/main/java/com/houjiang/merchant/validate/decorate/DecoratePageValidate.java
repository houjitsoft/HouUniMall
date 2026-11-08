package com.houjiang.merchant.validate.decorate;

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
public class DecoratePageValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "id", required = true)
    private Integer id;

    private String name;

    private Integer type;

    @NotNull(message = "pageData参数缺失")
    @Schema(description = "装修数据", required = true)
    private String data;

    @NotNull(message = "pageMeta")
    @Schema(description = "装修数据", required = true)
    private String common;

}
