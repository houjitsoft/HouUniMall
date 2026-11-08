package com.houjiang.merchant.vo.mall;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallShopSimplifyDetailVo implements Serializable {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "logo")
    private String logo;

    @Schema(description = "经营类目")
    @JsonProperty("shop_category")
    private JSONObject shopCategory;

    @Schema(description = "联系手机")
    private JSONArray mobile;
    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "类型 0自营 1入驻")
    private Integer type;

    @Schema(description = "类型 0自营 1入驻")
    @JsonProperty("type_text")
    private String typeText;
}
