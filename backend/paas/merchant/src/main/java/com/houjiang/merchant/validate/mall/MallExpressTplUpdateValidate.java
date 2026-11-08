package com.houjiang.merchant.validate.mall;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 运费模板参数
 * @author Amos
 */
@Data
@Schema(description = "运费模板更新参数")
public class MallExpressTplUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "店铺id")
    private Integer shopId;

    @NotNull(message = "name参数缺失")
    @Schema(description = "名称")
    private String name;

    @NotNull(message = "type参数缺失")
    @Schema(description = "计费方式 0件 1 重 2体积")
    private Integer type;

    @NotNull(message = "content参数缺失")
    @Schema(description = "配送内容")
    private JSONArray content;

    @Schema(description = "备注")
    private String remark;

}
