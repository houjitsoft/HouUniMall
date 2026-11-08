package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallGoods implements Serializable {

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

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品货号")
    private String code;

    @Schema(description = "商品描述")
    private String descript;

    @Schema(description = "商品分类")
    private String categoryIds;

    @Schema(description = "商家分类")
    private Integer shopCategoryId;

    @Schema(description = "主图")
    private String image;

    @Schema(description = "是否有视频")
    private Integer videoIs;

    @Schema(description = "视频来源 1素材库 2链接")
    private Integer videoFrom;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "视频地址")
    private String videoUri;

    @Schema(description = "规格类型 0单规格 1多规格")
    private Integer skuType;

    @Schema(description = "开启快递配送")
    private Integer isExpress;

    @Schema(description = "运费类型 0包邮 1运费模板 2 统一运费")
    private Integer expressType;

    @Schema(description = "运费模板ID")
    private Integer expressTplId;

    @Schema(description = "统一运费")
    private BigDecimal expressMoney;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "库存预警 0不做预警")
    private Integer stockWarning;

    @Schema(description = "最小价格")
    private BigDecimal minPrice;

    @Schema(description = "最大价格")
    private BigDecimal maxPrice;

    @Schema(description = "最小划线价")
    private BigDecimal minLinePrice;

    @Schema(description = "最大划线价")
    private BigDecimal maxLinePrice;

    @Schema(description = "总销量")
    private Integer saleNum;

    @Schema(description = "虚拟销量")
    private Integer virtualSaleNum;

    @Schema(description = "浏览量")
    private Integer visitNum;

    @Schema(description = "虚拟浏览量")
    private Integer virtualVisitNum;

    @Schema(description = "总销售额")
    private BigDecimal saleMoney;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态 0仓库 1上架")
    private Integer status;

    @Schema(description = "审核状态0待审核 1通过 2拒绝")
    private Integer auditStatus;

    @Schema(description = "审核内容")
    private String auditContent;

    @Schema(description = "店内推荐 1推荐 0不推荐")
    private Integer shopRecommend;

    @Schema(description = "回收站 1是 0否")
    private Integer isRecycle;

    @Schema(description = "详情")
    private String content;

    @Schema(description = "会员折扣 1是 0否")
    private Integer isDiscount;

    @Schema(description = "折扣规则 0默认 1单独折扣 2单独价格")
    private Integer discountType;

    @Schema(description = "参与分销 1是 0否")
    private Integer isDistribution;

    @Schema(description = "分销规则 0默认 1单独佣金比例 2单独设置佣金")
    private Integer distributionType;

}