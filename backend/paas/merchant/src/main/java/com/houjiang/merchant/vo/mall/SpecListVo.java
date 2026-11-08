package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SpecListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "规格名称ID")
    private Integer skuNameId;

    @Schema(description = "规格值")
    private String value;

    @Schema(description = "图片")
    private String image;
}
