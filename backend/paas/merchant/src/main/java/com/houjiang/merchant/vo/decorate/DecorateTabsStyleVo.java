package com.houjiang.merchant.vo.decorate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class DecorateTabsStyleVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "默认颜色")
    private String defaultColor;

    @Schema(description = "选中颜色")
    private String selectedColor;

}
