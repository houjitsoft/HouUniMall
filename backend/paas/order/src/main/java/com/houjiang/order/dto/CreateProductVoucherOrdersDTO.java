package com.houjiang.order.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName CreateProductVoucherOrdersDTO
 * @Description
 * @Author Amos
 * @Date 2024/9/9 17:16
 * @Version 1.0
 * @author Amos
 */
@Data
public class CreateProductVoucherOrdersDTO extends UserTokenBeanDTO {
    /**
     * 兑换券活动id
     */
    private Integer voucherTId;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 省id
     */
    private Integer orderReceiverProvinceId;
    /**
     * 市id
     */
    private Integer orderReceiverCityId;
    /**
     * 区id
     */
    private Integer orderReceiverAreaId;
    /**
     * 详细地址
     */
    private String orderReceiverAddress;
    /**
     * 收货人姓名
     */
    private String orderReceiverName;
    /**
     * 收货人联系方式
     */
    private String orderReceiverContact;
    /**
     * 订单来源  1PC 2h5 3小程序 4app
     */
    private String platForm;

}
