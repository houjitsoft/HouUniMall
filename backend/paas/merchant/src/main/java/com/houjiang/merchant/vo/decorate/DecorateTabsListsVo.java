package com.houjiang.merchant.vo.decorate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Data
public class DecorateTabsListsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "导航名称")
    private String name;

    @Schema(description = "已选图标")
    private String selected;

    @Schema(description = "未选图标")
    private String unselected;

    @Schema(description = "id")
    private Object link;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

}
