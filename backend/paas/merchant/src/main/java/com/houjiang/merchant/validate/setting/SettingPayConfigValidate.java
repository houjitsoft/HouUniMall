package com.houjiang.merchant.validate.setting;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author Amos
 */
@Data
public class SettingPayConfigValidate {

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID", required = true)
    private Integer id;

    @NotNull(message = "name参数缺失")
    @Schema(description = "模版名称", required = true)
    private String name;

    @Schema(description = "模版名称")
    private String icon;

    @Schema(description = "排序编号")
    private Integer sort;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "domain")
    private String domian;

    @Schema(description = "配置参数")
    private JSONObject config;

}
