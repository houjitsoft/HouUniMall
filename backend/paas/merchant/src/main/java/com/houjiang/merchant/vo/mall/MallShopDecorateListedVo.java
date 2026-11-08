package com.houjiang.merchant.vo.mall;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopDecorateListedVo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "ID")
    private Integer id;
    @Schema(description = "创建时间")
    private String createTime;
    @Schema(description = "修改时间")
    private String updateTime;
    @Schema(description = "店铺id")
    private Integer shopId;
    @Schema(description = "银行名称")
    private String name;
    @Schema(description = "类型 0店铺主页 1 客服 2底部导航")
    private Integer type;
    @Schema(description = "页面数据")
    private JSONArray content;
    @Schema(description = "公共配置")
    private JSONArray common;
    @Schema(description = "是否首页")
    private Integer isHome;
}
