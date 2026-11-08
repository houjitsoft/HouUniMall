package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName RegionVO
 * @Description
 * @Author Amos
 * @Date 2024/11/22 9:25
 * @Version 1.0
 * @author Amos
 */
@Data
public class RegionVO {

    /**
     * 全部编码
     */
    private String regionFullCodes;

    /**
     * 上级名称
     */
    private String parentName;

    /**
     * 区域编码
     */
    private String regionCode;

    /**
     * 首字母
     */
    private String initial;

    /**
     * 上级编码
     */
    private String parentCode;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 区域全称
     */
    private String regionFullName;

    /**
     * 区域名称
     */
    private String name;

    /**
     * ID
     */
    private String id;

    /**
     * 行政级别
     */
    private String regionLevel;

    /**
     * 经度
     */
    private String longitude;
}
