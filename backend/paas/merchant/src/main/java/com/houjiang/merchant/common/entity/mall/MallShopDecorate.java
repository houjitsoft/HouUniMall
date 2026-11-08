package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopDecorate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "店铺装修")
    private Integer shopId;

    @Schema(description = "类型 0店铺主页 1 客服")
    private Integer type;

    @Schema(description = "装修内容")
    private String content;

    @Schema(description = "公共配置")
    private String common;

    @Schema(description = "是否首页")
    private Integer isHome;

    @Schema(description = "页面名称")
    private String name;

}