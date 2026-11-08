package com.houjiang.merchant.vo.decorate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class DecorateTabbarVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "样式")
    private DecorateTabsStyleVo style;

    @Schema(description = "列表")
    private List<DecorateTabsListsVo> list;

}
