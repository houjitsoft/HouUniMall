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
public class MallGather implements Serializable {

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

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "采集链接")
    private String links;

    @Schema(description = "采集类型 0实物商品")
    private Integer type;

    @Schema(description = "平台分类")
    private Integer categoryId;

    @Schema(description = "店铺商品分类")
    private Integer shopCategoryId;

    @Schema(description = "采集数")
    private Integer nums;

    @Schema(description = "成功数")
    private Integer success;

    @Schema(description = "失败数")
    private Integer failed;

    @Schema(description = "消耗采集金额")
    private Integer gatherMoney;

    @Schema(description = "状态 0未执行 1已执行 2已完成")
    private Integer status;

}