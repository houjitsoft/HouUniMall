package com.houjiang.commons.dto.order;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 地址
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ManageShopAddressDetailDTO extends UserTokenBeanDTO {

    /**
     * 发货地址id
     */
    private Integer shippingAddressId;

}
