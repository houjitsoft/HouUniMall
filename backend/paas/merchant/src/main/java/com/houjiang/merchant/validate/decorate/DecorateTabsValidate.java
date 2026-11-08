package com.houjiang.merchant.validate.decorate;

import com.houjiang.merchant.vo.decorate.DecorateTabsListsVo;
import com.houjiang.merchant.vo.decorate.DecorateTabsStyleVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 底部导航装修参数
 * @author Amos
 */
@Data
@Schema(description = "底部装修参数")
public class DecorateTabsValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "样式")
    private DecorateTabsStyleVo style;

    @Schema(description = "列表")
    private List<DecorateTabsListsVo> list;

}
