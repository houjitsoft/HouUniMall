package com.houjiang.base.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName MerchantGoodsFormatDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/11 16:14
 * @Version 1.0
 * @author Amos
 */
@Data
public class MerchantGoodsFormatDTO extends UserTokenBeanDTO {
    private Integer id;

    private String name;

    /** 1顶部2底部 */
    private Integer position;

    private String shopId;

    private String content;
}
