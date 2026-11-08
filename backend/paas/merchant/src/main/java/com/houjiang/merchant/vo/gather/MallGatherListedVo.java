package com.houjiang.merchant.vo.gather;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.entity.mall.MallShopCategory;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsSkuListedVo;
import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.pt.GoodsPtVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGatherListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "采集链接")
    private JSONArray links;

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

    @Schema(description = "店铺分类")
    private MallShopCategory shopCategory;

    @Schema(description = "分类")
    private MallGoodsCategoryDetailVo category;
}
