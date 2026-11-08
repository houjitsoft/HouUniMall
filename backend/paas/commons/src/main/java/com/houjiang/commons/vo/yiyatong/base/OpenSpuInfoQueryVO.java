package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.util.List;

/**
 * @ClassName OpenSpuInfoQueryVO
 * @Description
 * @Author Amos
 * @Date 2024/11/6 15:45
 * @Version 1.0
 * @author Amos
 */

@Data
public class OpenSpuInfoQueryVO {

    /**
     * 是否为中国制造（示例：是/否）
     */
    private String isChina;

    /**
     * 库存类型编号
     */
    private Integer libType;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 视频链接或ID
     */
    private String video;

    /**
     * 库存量
     */
    private Integer inventory;

    /**
     * 类型编号
     */
    private Integer type;

    /**
     * 第三级分类名称
     */
    private String categoryName3;

    /**
     * 第一级分类名称
     */
    private String categoryName1;

    /**
     * 第二级分类名称
     */
    private String categoryName2;

    /**
     * 属性列表
     */
    private List<PropertysVO> propertys;

    /**
     * 限制区域列表
     */
    private List<String> limitArea;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 库存名称
     */
    private String libName;

    /**
     * 重量
     */
    private Integer weight;

    /**
     * 退货说明编号
     */
    private Integer returnInstructions;

    /**
     * 发货地点
     */
    private String deliveryPlace;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 第一级分类ID
     */
    private String categoryId1;

    /**
     * 详细图片链接列表
     */
    private List<String> detailImgList;

    /**
     * 容量
     */
    private Integer volume;

    /**
     * 第二级分类ID
     */
    private String categoryId2;

    /**
     * 第三级分类ID
     */
    private String categoryId3;

    /**
     * 限制区域类型
     */
    private Integer limitAreaType;

    /**
     * 是否限制区域（示例：是/否）
     */
    private String isLimitArea;

    /**
     * SPU类型
     */
    private String spuType;

    /**
     * 品牌ID
     */
    private String brandId;

    /**
     * 发货时间类型
     */
    private Integer deliveryTimeType;

    /**
     * 名称
     */
    private String name;

    /**
     * 是否包邮
     */
    private Integer freeExpress;

    /**
     * 零件号
     */
    private String npartno;

    /**
     * SPU ID
     */
    private String spuId;

    /**
     * 物流公司名称列表
     */
    private List<String> logisticsNameList;

    /**
     * 轮播图链接列表
     */
    private List<String> carouselImgList;

    /**
     * 状态
     */
    private String status;

    private List<OpenSkuInfoBySpuIdVO> skuList;
}
