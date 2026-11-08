package com.houjiang.commons.vo.third;

import lombok.Data;

/**
 * @Description: 店铺信息
 * @CreateDate: 2025/7/8 23:21
 * @author Amos
 */
@Data
public class ThirdShopVO {


    /**
     * 店铺id
     */
    private Integer id;

    /**
     * 智能云为客户编码
     */
    private String outShopCode;

    /**
     * 大区
     */
    private String regionName;
    /**
     * 办事处
     */
    private String officeName;
    /**
     * 片区
     */
    private String zoneName;
    /**
     * 省
     */
    private String province;
    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String area;

    /**
     * 销售队员
     */
    private String relationSaleName;
    /**
     * scrm店铺编码
     */
    private String shopCode;
    /**
     * 详细地址
     */
    private String shopDetailAddress;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺类型，1-餐厅/饭馆（用家）；2-批发/贸易商（批发）；3-超市/便利店（批发）；4-学校/工厂（批发）；5-电商（批发）
     */
    private Integer shopType;

    /**
     * 营业执照编号
     */
    private String businessLicenseRegNum;
}
