package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import com.houjiang.merchant.vo.mall.MallGoodsSkuVo;
import com.houjiang.merchant.vo.mall.SkuNameListVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品参数
 * @author Amos
 */
@Data
@Schema(description = "商品更新参数")
public class MallGoodsUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @NotNull(message = "请填写商品名称")
    @Schema(description = "商品名称")
    @Length(min = 1, max = 64, message = "商品名称最多64个字符")
    private String name;

    @NotNull(message = "请填写商品货号")
    @Schema(description = "商品货号")
    @Length(min = 1, max = 32, message = "商品货号最多32个字符")
    private String code;

    @NotNull(message = "descript参数缺失")
    @Schema(description = "商品描述")
    @Length(min = 0, max = 255, message = "商品描述最多255个字符")
    private String descript;
    @Schema(description = "店铺id")
    private Integer shopId;

    @NotNull(message = "请选择平台分类")
    @Schema(description = "平台分类")
    private List<Integer> categoryIds;

    @NotNull(message = "请选择商家分类")
    @Schema(description = "商家分类")
    private Integer shopCategoryId;

    @NotNull(message = "请选择商家分类")
    @Schema(description = "商家分类")
    @Size(min = 1, max = 10,  message = "轮播图最少上传1个，最多10个")
    private List<String> goodsImage;

    @IntegerContains(values = {0,1}, message = "规格类型错误")
    @NotNull(message = "请选择商品规格")
    @Schema(description = "规格类型 0单规格 1多规格")
    private Integer skuType;

    @NotNull(message = "请选择配送方式")
    @Schema(description = "配送方式")
    private List<Integer> goodsSendMode;

    /**
     * @NotNull(message = "image参数缺失")
     * @Schema(description = "主图")
     * private String image;
     * 
     */
    @NotNull(message = "请选择是否有视频")
    @Schema(description = "是否有视频")
    @IntegerContains(values = {0, 1}, message = "是否有视频错误")
    private Integer videoIs;

    @Schema(description = "视频来源 1素材库 2链接")
/** @IntegerContains(values = {1, 2}, message = "视频来源类型错误") */
    private Integer videoFrom;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "视频地址")
    private String videoUri;

/**
 * @NotNull(message = "isExpress参数缺失")
 * @Schema(description = "开启快递配送")
 * private Integer isExpress;
 * 
 */

    @NotNull(message = "请选择运费类型")
    @Schema(description = "运费类型 0包邮 1运费模板 2 统一运费")
    @IntegerContains(values = {0, 1, 2}, message = "运费类型参数错误")
    private Integer expressType;

    @Schema(description = "运费模板ID")
    private Integer expressTplId;

    private BigDecimal expressMoney;

    @Schema(description = "库存预警 0不做预警")
    private Integer stockWarning;

    private Integer ShopRecommend;

    /**
     * @NotNull(message = "minPrice参数缺失")
     * @Schema(description = "最小价格")
     * private BigDecimal minPrice;
     * 
     * @NotNull(message = "maxPrice参数缺失")
     * @Schema(description = "最大价格")
     * private BigDecimal maxPrice;
     * 
     * @NotNull(message = "minLinePrice参数缺失")
     * @Schema(description = "最小划线价")
     * private BigDecimal minLinePrice;
     * 
     * @NotNull(message = "maxLinePrice参数缺失")
     * @Schema(description = "最大划线价")
     * private BigDecimal maxLinePrice;
     * 
     * @NotNull(message = "saleNum参数缺失")
     * @Schema(description = "总销量")
     * private Integer saleNum;
     * 
     * @NotNull(message = "virtualSaleNum参数缺失")
     */
    @Schema(description = "虚拟销量")
    private Integer virtualSaleNum;
/**
 * @NotNull(message = "visitNum参数缺失")
 * @Schema(description = "浏览量")
 * private Integer visitNum;
 * 
 * @NotNull(message = "virtualVisitNum参数缺失")
 */
    @Schema(description = "虚拟浏览量")
    private Integer virtualVisitNum;
/**
 * @NotNull(message = "saleMoney参数缺失")
 * @Schema(description = "总销售额")
 * private BigDecimal saleMoney;
 * 
 */
    @Schema(description = "排序")
    private Integer sort;

    @NotNull(message = "请选择销售状态")
    @Schema(description = "销售状态")
    @IntegerContains(values = {0, 1}, message = "销售状态类型错误")
    private Integer status;
    @NotEmpty(message = "请填写商品详情")
    @Schema(description = "详情")
    private String content;

    @Schema(description = "规格值列表")
    private List<MallGoodsSkuVo> sku;

    @Schema(description = "服务规格值列表")
    private List<SkuNameListVo> skuNameList;
}
