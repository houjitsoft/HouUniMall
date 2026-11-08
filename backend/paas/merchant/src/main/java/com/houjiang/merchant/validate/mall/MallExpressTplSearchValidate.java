package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallExpressTplSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "计费方式 0件 1 重 2体积")
    private Integer type;

    @Schema(description = "配送内容")
    private String content;

    @Schema(description = "备注")
    private String remark;

}
