package com.houjiang.merchant.vo.mall;

import com.alibaba.fastjson2.JSONArray;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallGoodsCommentListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;
    @Schema(description = "商品ID")
    @JsonProperty("goods_sku_id")
    private Integer goodsId;
    @Schema(description = "商品skuID")
    @JsonProperty("goods_id")
    private Integer goodsSkuId;
    @Schema(description = "创建时间")
    @JsonProperty("create_time")
    private String createTime;
    @Schema(description = "评分")
    private Integer score;
    @Schema(description = "回复内容")
    private String content;
    @Schema(description = "已回复")
    @JsonProperty("is_reply")
    private Integer isReply;
    @Schema(description = "回复内容")
    @JsonProperty("reply_content")
    private String replyContent;

    @Schema(description = "是否显示")
    @JsonProperty("is_show")
    private Integer isShow;

    @Schema(description = "描述评分")
    @JsonProperty("describe_score")
    private Integer describeScore;
    @Schema(description = "服务评分")
    @JsonProperty("service_score")
    private Integer serviceScore;
    @Schema(description = "物流评分")
    @JsonProperty("logistics_score")
    private Integer logisticsScore;
    @Schema(description = "评论图片")
    private JSONArray images;
    @Schema(description = "用户昵称")
    private String nickname;
    @Schema(description = "编号")
    @JsonProperty("user_sn")
    private Integer sn;
    @Schema(description = "商品名称")
    @JsonProperty("goods_name")
    private String goodsName;
    @Schema(description = "商品图片")
    @JsonProperty("goods_image")
    private String goodsImage;
    @Schema(description = "商品规格值名称")
    @JsonProperty("goods_sku_value")
    private JSONArray goodsSkuValue;

    @Schema(description = "评论等级")
    @JsonProperty("level")
    private String level;

    @Schema(description = "评论等级字符串")
    @JsonProperty("level_text")
    private String levelText;
}
