package com.houjiang.commons.dto.yiyatong;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * @ClassName SpuPageQueryDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/6 14:55
 * @Version 1.0
 * @author Amos
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class SpuPageQueryDTO extends PageBaseHelper {

    /**
     * 商品是否含视频： -1、全部 1、只含视频，默认 -1
     */
    private String hasVideo;
    /**
     * 选品开始时间，格式 为 'yyyy-MM-dd HH:mm:ss'
     */
    private String createStartTime;
    /**
     * 页码，默认 1
     */
    private String pageIndex;
    /**
     * 选品截止时间，格式 为 'yyyy-MM-dd HH:mm:ss'
     */
    private String createEndTime;
    /**
     * 品牌ID
     */
    private String brandId;
    /**
     * 每页大小，默认 500，最大1000
     */
    private String pageSize;
    /**
     * 一级分类ID
     */
    private String firstCategoryId;
    /**
     * 二级分类ID
     */
    private String secondCategoryId;
    /**
     * 三级分类ID
     */
    private String thirdCategoryId;
    /**
     * 商品状态： -1.全部，1.已上架，2.已下架，3.已失效，默认 1
     */
    private String status;

    /**
     * 是否已代发
     */
    private Integer dropShipping;

    /**
     * 利润率 排序 0 不生效 1 升序  2 降序
     */
    private Integer profitMarginSorting;
    /**
     * 协议价 排序 0 不生效 1 升序  2 降序
     */
    private Integer agreementPriceSorting;

}
