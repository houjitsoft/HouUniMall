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
public class MallGoodsComment implements Serializable {

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

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单商品ID")
    private Integer orderGoodsId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "商品skuID")
    private Integer goodsSkuId;

    @Schema(description = "评分")
    private Integer score;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "已回复")
    private Integer isReply;

    @Schema(description = "回复内容")
    private String replyContent;

    @Schema(description = "描述评分")
    private Integer describeScore;

    @Schema(description = "服务评分")
    private Integer serviceScore;

    @Schema(description = "物流评分")
    private Integer logisticsScore;

    @Schema(description = "有图片")
    private Integer hasImages;

    @Schema(description = "评论图片")
    private String images;

}