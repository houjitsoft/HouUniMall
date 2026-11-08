package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName OrderInvoiceAddressEntity
 * @Description 发票地址实体类
 * @Author Amos
 * @Date 2022/7/12 11:20
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderInvoiceAddressVO {
    /**
     * 地址ID
     */
    private Integer addressId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userMobile;

    /**
     * 地址所在省份ID
     */
    private Integer addressProvince;

    /**
     * 地址所在城市ID
     */
    private Integer addressCity;

    /**
     * 地址所在地区ID
     */
    private Integer addressArea;

    /**
     * 地址详细信息
     */
    private String addressDetail;

    /**
     * 完整地址，可能包括楼号、单元号等信息
     */
    private String completeAddress;

    /**
     * 预完成地址，可能在用户填写地址时自动填充部分信息
     */
    private String preCompleteAddress;
}
