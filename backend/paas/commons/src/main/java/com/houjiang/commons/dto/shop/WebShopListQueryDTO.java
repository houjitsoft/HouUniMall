package com.houjiang.commons.dto.shop;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WebShopListQueryDTO extends PageHelper {

    /**
     * 1默认排序，2收藏量排序
     */
    private Integer orderType;

    /**
     * 店铺自营'true'自营,'false'
     */
    private String shopSelfSupport;

    /**
     * 所在地，使用最后一级分类
     */
    private Integer districtId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺类型
     */
    private Integer shopType;

    /**
     * 地区集合
     */
    private List<Integer> districtList;


}

