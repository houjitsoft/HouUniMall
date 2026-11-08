package com.houjiang.merchant.validate.area;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class RegionSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索关键字")
    @NotNull(message = "搜索关键字不能为空")
    private String tencent_keyword;

    @Schema(description = "搜索区域")
    @NotNull(message = "搜索区域不能为空")
    private String tencent_boundary;

    @Schema(description = "腾讯地图开发密钥")
    @NotNull(message = "腾讯地图开发密钥不能为空")
    private String tencent_key;

}
