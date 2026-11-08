package com.houjiang.merchant.vo.ms;

import com.houjiang.merchant.vo.mall.MallGoodsDetailVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MsDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

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
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "审核状态 0待提交 10待审核 20审核成功 30审核失败")
    private Integer auditStatus;

    @Schema(description = "审核状态 0待提交 10待审核 20审核成功 30审核失败")
    private String auditStatusText;

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
    @Schema(description = "可以关闭")
    private Integer canClose;
    @Schema(description = "可以编辑")
    private Integer canEdit;
    @Schema(description = "可以打开")
    private Integer canOpen;
    @Schema(description = "可以提交")
    private Integer canSubmit;
    @Schema(description = "商品id")
    private MallGoodsDetailVo goods;
    @Schema(description = "价格")
    private String priceText;
    @Schema(description = "状态")
    private String statusText;

    @Schema(description = "秒杀列表")
    private List<MsSkuListedVo> msSkuList;
}
