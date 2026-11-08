package com.houjiang.merchant.validate.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class ArtCateSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "是否显示")
    private Integer isShow;

}
