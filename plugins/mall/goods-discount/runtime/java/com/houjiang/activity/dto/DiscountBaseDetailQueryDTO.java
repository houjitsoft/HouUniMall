package com.houjiang.activity.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName DiscountBaseDetailQueryDTO
 * @Author Amos
 * @Date 2023/7/3 15:32
 * @Version 1.0
 * @author Amos
 */
@Data
public class DiscountBaseDetailQueryDTO extends UserTokenBeanDTO {
    /**
     * 限时编号
     */
    private Integer discountId;

    /**
     * 商品id
     */
    private Integer commonId;


}
