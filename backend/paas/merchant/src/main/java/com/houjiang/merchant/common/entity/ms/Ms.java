package com.houjiang.merchant.common.entity.ms;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class Ms implements Serializable {

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

    @Schema(description = "商品id")
    private Integer goodsId;

    @Schema(description = "最大秒杀价")
    private Integer maxPrice;

    @Schema(description = "最小秒杀价")
    private Integer minPrice;

    @Schema(description = "状态 0未开始 1进行中 2已结束")
    private Integer status;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "审核状态 0待提交 10待审核 20审核成功 30审核失败")
    private Integer auditStatus;

    @Schema(description = "审核说明")
    private String auditContent;

    @Schema(description = "下单限制 0不限制")
    private Integer orderLimit;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "剩余库存")
    private Integer stock;

    @Schema(description = "总销量")
    private Integer saleNum;

    @Schema(description = "浏览量")
    private Integer visitNum;

}