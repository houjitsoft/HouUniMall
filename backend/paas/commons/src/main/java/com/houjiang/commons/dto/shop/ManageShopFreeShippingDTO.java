package com.houjiang.commons.dto.shop;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * @ClassName ManageShopFreeShippingDTO
 * @Description 更新店铺免额度
 * @Author Amos
 * @Date 2023/7/5 11:02
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ManageShopFreeShippingDTO extends UserTokenBeanDTO {
    private BigDecimal shopFreeShipping;

//    private Integer shopId;
}
