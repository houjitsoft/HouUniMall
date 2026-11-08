package com.houjiang.merchant.vo.mall;

import com.alibaba.fastjson2.JSONArray;
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
public class MallGoodsOtherListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "视频来源")
    private JSONArray videoFromArr;

    @Schema(description = "sku类型")
    private JSONArray skuTypeList;
    @Schema(description = "配送方式")
    private JSONArray goodsSendModeList;

    @Schema(description = "配送类型")
    private JSONArray expressTypeList;

    @Schema(description = "状态列表")
    private JSONArray statusList;

    @Schema(description = "配送计算类型")
    private List<MallExpressTplGoodsVo>  expressList;
    @Schema(description = "店铺商品分类")
    private List<MallShopGoodsCategoryOtherListedVo> shopGoodsCategory;

    @Schema(description = "平台分类")
    private List<MallGoodsCategoryListedVo> categoryList;
}
