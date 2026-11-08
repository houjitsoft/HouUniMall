package com.houjiang.base.vo.district;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ShopAllBaseDistrict2VO
 * @Description TODO
 * @Author Amos
 * @Date 2023/7/3 10:55
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShopAllBaseDistrict2VO {
    /**
     * 分区ID
     */
    private Integer districtId;

    /**
     * 分区名称
     */
    private String districtName;

    /**
     * 地区层级
     */
    private Integer districtIsLevel;

    /**
     * 分区子级
     */
    private List<ShopAllBaseDistrict2VO> childDistrict;
}
