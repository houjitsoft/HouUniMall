package com.houjiang.merchant.validate.config;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ConfigSetServiceValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "h5")
    private JSONObject h5;
    @Schema(description = "oa")
    private JSONObject oa;
    @Schema(description = "mnp")
    private JSONObject mnp;

    private Integer shopId;
}
